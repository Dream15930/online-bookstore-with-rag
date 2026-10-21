"""统一注册数据库工具与知识库工具，供 chatbot / api 使用"""
from db_tools import ALL_TOOLS as DB_TOOLS, TOOL_MAP as DB_TOOL_MAP
from rag_tools import ALL_TOOLS as RAG_TOOLS, TOOL_MAP as RAG_TOOL_MAP

ALL_TOOLS = DB_TOOLS + RAG_TOOLS
TOOL_MAP = {**DB_TOOL_MAP, **RAG_TOOL_MAP}
assert len(ALL_TOOLS) == len(TOOL_MAP), "工具名冲突"
