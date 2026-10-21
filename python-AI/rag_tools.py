"""知识库检索工具（供 LLM 调用）"""
from langchain_core.tools import tool

from rag.retriever import retrieve_and_rerank


@tool
def search_knowledge_base(query: str) -> str:
    """检索书店知识库，用于回答配送与物流、退换货、支付、发票、会员与优惠、
    积分、账号安全、隐私政策，以及图书导读与推荐文案等静态内容。
    参数:
        query: 用户的问题或关键词。
    返回: 最相关的知识库片段（含来源文件名）；若无命中返回提示语。
    """
    try:
        hits = retrieve_and_rerank(query)
    except Exception as e:
        return f"知识库检索失败:{e}"
    if not hits:
        return "知识库暂无相关内容。"
    return "\n\n".join(f"[来源:{source}]\n{text}" for text, source, _ in hits)


ALL_TOOLS = [search_knowledge_base]
TOOL_MAP = {t.name: t for t in ALL_TOOLS}
