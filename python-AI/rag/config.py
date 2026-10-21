"""RAG 检索配置：路径、模型、检索参数"""
import os
from pathlib import Path

# python-AI 项目根目录（rag/ 的上一级）
BASE_DIR = Path(__file__).resolve().parent.parent

KNOWLEDGE_DIR = BASE_DIR / "knowledge"
CHROMA_DIR = BASE_DIR / "chroma_db"

# 向量化与重排模型（本地 sentence-transformers，首次运行会从 HuggingFace 下载）
EMBEDDING_MODEL = os.getenv("RAG_EMBEDDING_MODEL", "shibing624/text2vec-base-chinese")
RERANK_MODEL = os.getenv("RAG_RERANK_MODEL", "cross-encoder/mmarco-mMiniLMv2-L12-H384-v1")

COLLECTION_NAME = "bookstore_knowledge"

# 检索参数：向量召回 5 条 → 重排取前 3 条
TOP_K_RETRIEVE = int(os.getenv("RAG_TOP_K_RETRIEVE", "5"))
TOP_K_RERANK = int(os.getenv("RAG_TOP_K_RERANK", "3"))
