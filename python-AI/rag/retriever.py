"""检索与重排"""
from typing import List, Tuple

from .config import TOP_K_RERANK, TOP_K_RETRIEVE
from .indexer import embed_chunk, get_collection, get_cross_encoder


def retrieve(query: str, top_k: int = TOP_K_RETRIEVE) -> List[Tuple[str, str, int]]:
    """向量召回，返回 [(text, source, chunk_index)]。"""
    collection = get_collection()
    count = collection.count()
    if count == 0:
        return []

    result = collection.query(
        query_embeddings=[embed_chunk(query)],
        n_results=min(top_k, count),
        include=["documents", "metadatas"],
    )
    documents = result["documents"][0]
    metadatas = result["metadatas"][0]
    return [
        (doc, meta.get("source", "未知"), meta.get("chunk_index", -1))
        for doc, meta in zip(documents, metadatas)
    ]


def rerank(
    query: str, candidates: List[Tuple[str, str, int]], top_k: int = TOP_K_RERANK
) -> List[Tuple[str, str, int]]:
    """CrossEncoder 打分，降序取前 top_k。"""
    if not candidates:
        return []
    pairs = [(query, text) for text, _, _ in candidates]
    scores = get_cross_encoder().predict(pairs)
    ranked = sorted(zip(candidates, scores), key=lambda item: item[1], reverse=True)
    return [candidate for candidate, _ in ranked[:top_k]]


def retrieve_and_rerank(query: str) -> List[Tuple[str, str, int]]:
    """召回 + 重排，返回最终命中片段。"""
    return rerank(query, retrieve(query))
