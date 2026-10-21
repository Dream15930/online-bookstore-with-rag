"""向量索引：模型/向量库懒加载单例 + 建索引（带 md5 幂等）

重依赖（torch / sentence-transformers / chromadb）全部延迟到首次使用时加载，
避免 import 本模块就拖慢服务启动。
"""
import hashlib
import threading
from typing import List

from .config import CHROMA_DIR, COLLECTION_NAME, EMBEDDING_MODEL, RERANK_MODEL
from .loader import iter_knowledge_files, split_into_chunks

_lock = threading.Lock()
_embedding_model = None
_cross_encoder = None
_collection = None


def get_embedding_model():
    global _embedding_model
    if _embedding_model is None:
        with _lock:
            if _embedding_model is None:
                from sentence_transformers import SentenceTransformer

                _embedding_model = SentenceTransformer(EMBEDDING_MODEL)
    return _embedding_model


def get_cross_encoder():
    global _cross_encoder
    if _cross_encoder is None:
        with _lock:
            if _cross_encoder is None:
                from sentence_transformers import CrossEncoder

                _cross_encoder = CrossEncoder(RERANK_MODEL)
    return _cross_encoder


def get_collection():
    global _collection
    if _collection is None:
        with _lock:
            if _collection is None:
                import chromadb

                CHROMA_DIR.mkdir(parents=True, exist_ok=True)
                client = chromadb.PersistentClient(path=str(CHROMA_DIR))
                # embedding_function=None：我们始终显式传入向量，
                # 避免 chroma 懒加载默认 ONNX embedding（离线会失败/变慢）
                _collection = client.get_or_create_collection(
                    name=COLLECTION_NAME, embedding_function=None
                )
    return _collection


def embed_chunk(chunk: str) -> List[float]:
    return get_embedding_model().encode(chunk).tolist()


def file_hash(file_path: str) -> str:
    """计算文件 md5，内容不变则 hash 不变。"""
    with open(file_path, "rb") as f:
        return hashlib.md5(f.read()).hexdigest()


def save_embeddings(chunks: List[str], source_name: str, file_path: str) -> None:
    """写入向量。文件内容未变则跳过；已变则删旧写新。"""
    collection = get_collection()
    current_hash = file_hash(file_path)

    existing = collection.get(where={"source": source_name}, include=["metadatas"])
    if existing["ids"]:
        old_hash = (
            existing["metadatas"][0].get("file_hash", "")
            if existing["metadatas"]
            else ""
        )
        if old_hash == current_hash:
            print(f"  [{source_name}] 内容未变，跳过建索引")
            return
        collection.delete(ids=existing["ids"])
        print(f"  [{source_name}] 文件已更新，重新建索引")

    embeddings = [embed_chunk(chunk) for chunk in chunks]
    ids = [f"{source_name}_{i}" for i in range(len(chunks))]
    metadatas = [
        {"source": source_name, "file_hash": current_hash, "chunk_index": i}
        for i in range(len(chunks))
    ]
    collection.add(
        documents=chunks,
        embeddings=embeddings,
        ids=ids,
        metadatas=metadatas,
    )
    print(f"  [{source_name}] 写入 {len(chunks)} 条")


def sync_index(active_sources: set) -> None:
    """清理知识库中已被删除文件的残留向量。"""
    collection = get_collection()
    existing = collection.get(include=["metadatas"])
    stale_ids: dict = {}
    for doc_id, meta in zip(existing["ids"], existing["metadatas"]):
        source = meta.get("source")
        if source and source not in active_sources:
            stale_ids.setdefault(source, []).append(doc_id)
    for source, ids in stale_ids.items():
        collection.delete(ids=ids)
        print(f"  [{source}] 文件已删除，清理 {len(ids)} 条向量")


def build_index() -> dict:
    """遍历 knowledge/ 建索引，返回统计信息。"""
    files = iter_knowledge_files()
    if not files:
        print("  [警告] knowledge/ 目录为空，未建立索引")
        return {"files": 0, "chunks": 0}

    total_chunks = 0
    for file_path in files:
        try:
            chunks = split_into_chunks(str(file_path))
            save_embeddings(chunks, source_name=file_path.name, file_path=str(file_path))
            total_chunks += len(chunks)
        except Exception as e:
            print(f"  [错误] 加载失败 {file_path.name}: {e}")

    sync_index({f.name for f in files})
    return {"files": len(files), "chunks": total_chunks}


if __name__ == "__main__":
    build_index()
