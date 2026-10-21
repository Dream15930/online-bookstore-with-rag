"""
网上书店智能客服 - FastAPI Web 服务
提供 SSE 流式聊天接口
"""

import asyncio
import json
import logging
import os
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

from chatbot import (
    SYSTEM_PROMPT, llm_deepseek, keep_recent_messages,
    keep_recent_messages_with_tools, get_llm_with_tools, collect_stream_message,
)
from session_manager import SessionManager
from tool_registry import TOOL_MAP

logger = logging.getLogger("bookstore.chat")


@asynccontextmanager
async def lifespan(app: FastAPI):
    """启动时预热 RAG 模型并建立/更新知识库索引；失败则降级（仅数据库工具可用）。"""
    try:
        from rag.indexer import build_index

        stats = await asyncio.to_thread(build_index)
        logger.info("知识库索引完成: %s", stats)
        app.state.rag_ready = True
    except Exception as e:
        logger.error("知识库初始化失败，已降级（仅数据库工具可用）: %s", e)
        app.state.rag_ready = False
    yield


app = FastAPI(title="书香在线书店 - 智能客服", lifespan=lifespan)

# CORS 配置：通过环境变量 CORS_ORIGINS 指定允许的前端域名（逗号分隔）
# 示例: https://bookstore.example.com,https://admin.example.com
# 开发环境默认: http://localhost:3000
cors_origins = os.getenv("CORS_ORIGINS", "http://localhost:3000").split(",")
app.add_middleware(
    CORSMiddleware,
    allow_origins=cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 全局会话管理器：30 分钟过期，每 5 分钟清理
session_manager = SessionManager(timeout_seconds=1800, cleanup_interval=300)


# 工具调用时向前端展示的提示文案（按工具类型分流）
TOOL_STATUS_LABELS = {
    "search_knowledge_base": "正在查阅知识库...",
}


def _tool_status_text(tool_name: str) -> str:
    return TOOL_STATUS_LABELS.get(tool_name, f"正在查询数据库({tool_name})...")


# ----- 请求/响应模型 -----

class ChatRequest(BaseModel):
    session_id: str
    message: str


class SessionMessagesRequest(BaseModel):
    session_id: str


# ----- API 端点 -----

@app.get("/api/chat/session")
async def create_session():
    """创建新的聊天会话"""
    session_id = session_manager.create_session(SYSTEM_PROMPT)
    return {"session_id": session_id}


@app.post("/api/chat/stream")
async def streaming_chat(request: ChatRequest):
    """
    SSE 流式聊天端点（支持 Tool Calling 查询数据库）
    接收 session_id 和用户消息，LLM 可自动调用数据库工具获取真实数据
    """
    session_id = request.session_id
    message = request.message
    MAX_TOOL_ROUNDS = 5  # 最多工具调用轮次，防止无限循环

    # 会话不存在或已过期则自动创建
    if session_manager.get_session(session_id) is None:
        session_id = session_manager.create_session(SYSTEM_PROMPT)

    # 添加用户消息
    session_manager.add_message(session_id, "user", message)

    async def event_generator():
        try:
            # 发送初始事件
            yield f"data: {json.dumps({'status': 'started'})}\n\n"

            # 获取带工具的 LLM 实例
            llm_with_tools = get_llm_with_tools()

            tool_rounds = 0
            while tool_rounds < MAX_TOOL_ROUNDS:
                tool_rounds += 1

                # 从 session 获取最新消息
                raw_messages = session_manager.get_messages(session_id)
                if raw_messages is None:
                    yield f"data: {json.dumps({'error': '会话不存在'})}\n\n"
                    return

                trimmed = keep_recent_messages_with_tools(raw_messages, max_pairs=10)

                # 流式收集完整 AIMessage
                full_msg = collect_stream_message(llm_with_tools, trimmed)

                # 检查是否调用了工具
                if full_msg.tool_calls and len(full_msg.tool_calls) > 0:
                    # 保存 AI 的工具调用消息到 session
                    session_manager.add_message(
                        session_id, "assistant",
                        full_msg.content or "",
                    )
                    # 也保存 tool_calls 到消息中（用自定义字段）
                    raw_messages = session_manager.get_messages(session_id)
                    if raw_messages:
                        # 标记消息含 tool_calls
                        raw_messages[-1]["tool_calls"] = [
                            {
                                "id": tc.get("id", ""),
                                "name": tc.get("name", ""),
                                "args": tc.get("args", {}),
                            }
                            for tc in full_msg.tool_calls
                        ]

                    # 执行每个工具调用
                    for tool_call in full_msg.tool_calls:
                        tool_name = tool_call.get("name", "")
                        tool_args = tool_call.get("args", {})
                        tool_call_id = tool_call.get("id", "")

                        # 通知前端正在查询
                        yield f"data: {json.dumps({'tool_status': _tool_status_text(tool_name)}, ensure_ascii=False)}\n\n"
                        await asyncio.sleep(0)

                        # 执行工具（丢到线程，避免阻塞事件循环）
                        if tool_name in TOOL_MAP:
                            try:
                                result = await asyncio.to_thread(
                                    TOOL_MAP[tool_name].invoke, tool_args
                                )
                            except Exception as tool_err:
                                result = f"工具执行出错: {str(tool_err)}"
                        else:
                            result = f"未知工具: {tool_name}"

                        # 将工具结果加入 session
                        session_manager.add_message(
                            session_id, "tool",
                            str(result),
                        )
                        # 标记 tool_call_id（用于消息配对）
                        raw_msgs = session_manager.get_messages(session_id)
                        if raw_msgs:
                            raw_msgs[-1]["tool_call_id"] = tool_call_id

                    # 继续循环，让 LLM 根据工具结果生成回复
                    continue

                # 没有工具调用 — 这是最终文本回复
                reply_content = full_msg.content if full_msg.content else ""

                if reply_content:
                    yield f"data: {json.dumps({'token': reply_content}, ensure_ascii=False)}\n\n"
                    await asyncio.sleep(0)

                # 将最终回复存入 session
                session_manager.add_message(session_id, "assistant", reply_content)

                # 发送完成信号
                yield f"data: {json.dumps({'done': True})}\n\n"
                return

            # 超过最大工具轮次
            yield f"data: {json.dumps({'error': '查询次数过多，请稍后再试'}, ensure_ascii=False)}\n\n"

        except Exception as e:
            import traceback
            traceback.print_exc()
            yield f"data: {json.dumps({'error': str(e)}, ensure_ascii=False)}\n\n"

    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",
        },
    )


@app.post("/api/chat/messages")
async def get_session_messages(request: SessionMessagesRequest):
    """获取会话历史消息（用于恢复对话）"""
    messages = session_manager.get_messages(request.session_id)
    if messages is None:
        return {"messages": [], "error": "会话不存在或已过期"}
    # 过滤掉 system 消息，只返回对话
    conversation = [m for m in messages if m["role"] != "system"]
    return {"messages": conversation}


@app.get("/api/chat/health")
async def health_check():
    """健康检查"""
    return {
        "status": "ok",
        "active_sessions": session_manager.active_count,
        "rag_ready": getattr(app.state, "rag_ready", False),
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
