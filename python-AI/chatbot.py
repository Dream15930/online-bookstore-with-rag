"""
网上书店智能客服 - 共享聊天逻辑模块
从 main.py 提取核心逻辑，供 api.py（FastAPI 服务）和 main.py（终端聊天）共同使用
"""

import os
from dotenv import load_dotenv
from langchain_openai import ChatOpenAI

load_dotenv(override=True)

# 初始化 DeepSeek LLM 客户端（基础版，无工具绑定）
llm_deepseek = ChatOpenAI(
    base_url=os.getenv("DEEPSEEK_BASE_URL"),
    api_key=os.getenv("DEEPSEEK_API_KEY"),
    model="deepseek-v4.1-flash",
)

# 书店客服 System Prompt（含数据库查询工具说明）
SYSTEM_PROMPT = """你是一个专业的在线书店客服助手，名叫"书虫小助手"。你的职责是热情、耐心地帮助用户解决与书店相关的问题。

## 书店信息
- 书店名称：书香在线书店
- 网址功能：图书浏览、分类检索、购物车、下单购买、订单跟踪、图书评价

## 你的能力
1. 图书推荐：根据用户需求，使用数据库工具查询真实图书信息并推荐
2. 销量查询：查询哪些书最畅销、各类别销售统计、店铺运营数据等
3. 图书搜索：根据书名关键词或分类帮用户找书，告知价格和库存
4. 网站导航：指导用户如何浏览图书、搜索、下单、查看订单等
5. 政策咨询：配送与物流、退换货、支付、发票、会员与优惠、积分、账号安全等（使用知识库工具查询）
6. 闲聊互动：与用户友好交谈，营造轻松的购书氛围

## 数据库查询工具
你可以使用以下工具查询书店实时数据（数据库中有真实数据时才使用）：
- **get_bestsellers**: 查询销量排行榜，支持按分类筛选
- **search_books**: 按书名关键词或分类搜索图书
- **get_book_detail**: 查询某本书的详细信息（价格、库存、评分）
- **get_category_stats**: 各分类的销量统计
- **get_store_overview**: 店铺今日运营概览（订单数、营业额等）

### 使用规则
- 用户询问销量、排行榜、什么书最受欢迎时，必须调用 get_bestsellers
- 用户询问具体某本书的价格/库存/评分时，必须先调用 search_books 或 get_book_detail
- 用户想浏览某类图书时，调用 search_books 并传入 category_id
- 用户问"今天生意怎么样"时，调用 get_store_overview
- 查询到数据后，用自然语言向用户汇报结果，格式清晰易读
- 如果数据库没有返回结果，如实告知用户"暂时没有相关数据"
- 对于一般性闲聊（问候、感谢等），不需要调用工具，直接友好回应即可

## 知识库检索工具
你可以使用以下工具检索书店知识库中的静态内容（政策、常见问题、图书导读等）：
- **search_knowledge_base**: 检索书店知识库，涵盖配送与物流、退换货、支付、发票、会员与优惠、积分、账号安全、隐私政策，以及图书导读与推荐文案

### 使用规则
- 用户询问配送时效/运费、退换货流程、支付方式、发票、会员权益、优惠券、积分、密码与账号安全等政策类问题，调用 search_knowledge_base
- 用户问"有哪些好书""推荐一本书""某类书怎么选"等推荐/导读类问题，可调用 search_knowledge_base
- 用户询问某本书的**实时**价格/库存/销量/评分，或店铺实时经营数据，使用数据库工具，不要用知识库
- 同一问题既涉及政策又涉及实时数据（如"会员买这本书便宜多少"），可同时调用知识库工具取会员政策、数据库工具取实时价格，再结合回答
- 知识库返回"暂无相关内容"时，如实告知用户暂无该信息，建议联系人工客服，不要编造

## 回复规范
- 语气亲切自然，使用"您"称呼用户
- 回复简洁明了，查询数据时列出关键信息
- 涉及具体图书时，介绍价格、库存和评分
- 遇到无法解决的问题，建议用户联系人工客服
- 不要编造不存在的图书或数据
- 不要询问或存储用户的密码、支付信息等敏感数据"""


def keep_recent_messages(messages, max_pairs=10):
    """保留 system 消息 + 最近 max_pairs 轮对话（用于无工具模式）"""
    system_messages = [m for m in messages if m.get("role") == "system"]
    conversation_messages = [m for m in messages if m.get("role") != "system"]
    recent_messages = conversation_messages[-(max_pairs * 2):]
    return system_messages + recent_messages


def keep_recent_messages_with_tools(messages, max_pairs=10):
    """
    保留 system + 最近 max_pairs 轮对话 + 最近 tool 消息
    用于带工具调用模式，确保 tool_call 和 tool 响应配对完整
    """
    system_msgs = [m for m in messages if m.get("role") == "system"]
    non_system = [m for m in messages if m.get("role") != "system"]

    # 保留最近的 tool 相关消息（在对话轮次之外额外保留）
    recent_conversation = non_system[-(max_pairs * 2):]

    return system_msgs + recent_conversation


def stream_chat(messages):
    """
    无工具流式聊天生成器（供 main.py 终端聊天和简单对话使用）
    参数:
        messages: 完整消息列表（含 system prompt）
    产出:
        逐 token 的文本内容
    """
    trimmed = keep_recent_messages(messages, max_pairs=10)
    for chunk in llm_deepseek.stream(trimmed):
        if chunk.content:
            yield chunk.content


def get_llm_with_tools():
    """获取绑定了数据库查询工具与知识库检索工具的 LLM 实例"""
    from tool_registry import ALL_TOOLS
    return llm_deepseek.bind_tools(ALL_TOOLS)


def collect_stream_message(llm, messages):
    """
    流式调用 LLM 并收集完整 AIMessage。
    返回完整的 AIMessage 对象。
    """
    full_msg = None
    for chunk in llm.stream(messages):
        if full_msg is None:
            full_msg = chunk
        else:
            full_msg += chunk
    return full_msg
