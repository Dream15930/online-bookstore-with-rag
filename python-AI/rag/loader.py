"""知识库文档加载与切分"""
from pathlib import Path
from typing import List

from .config import KNOWLEDGE_DIR


def split_into_chunks(doc_file: str) -> List[str]:
    """按空行（\\n\\n）把文档切成 chunk，去除首尾空白并过滤空块。"""
    with open(doc_file, "r", encoding="utf-8") as f:
        content = f.read()
    chunks = [chunk.strip() for chunk in content.split("\n\n")]
    return [chunk for chunk in chunks if chunk]


def iter_knowledge_files() -> List[Path]:
    """列出 knowledge/ 下的 .md / .txt 文件（目录不存在返回空列表）。"""
    if not KNOWLEDGE_DIR.exists():
        return []
    files = list(KNOWLEDGE_DIR.glob("*.md")) + list(KNOWLEDGE_DIR.glob("*.txt"))
    return sorted(files)
