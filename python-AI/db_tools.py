"""
数据库查询工具模块
提供 MySQL 只读查询函数，用 LangChain Tool 包装，供 LLM 调用
"""

import os
import pymysql
from pymysql.cursors import DictCursor
from langchain_core.tools import tool


def _get_connection():
    """创建 MySQL 只读连接"""
    return pymysql.connect(
        host=os.getenv("DB_HOST", "localhost"),
        port=int(os.getenv("DB_PORT", "3306")),
        user=os.getenv("DB_USERNAME", "root"),
        password=os.getenv("DB_PASSWORD", "root"),
        database=os.getenv("DB_NAME", "bookstore_new"),
        charset="utf8mb4",
        cursorclass=DictCursor,
    )


def _query(sql, params=None):
    """执行只读查询，返回字典列表"""
    conn = _get_connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute(sql, params)
            return cursor.fetchall()
    finally:
        conn.close()


# ============ 工具函数 ============


@tool
def get_bestsellers(limit: int = 5, category_id: int = None) -> str:
    """
    查询图书销量排行榜（按订单中实际售出数量排名）。
    参数:
        limit: 返回前几名，默认 5
        category_id: 可选，按分类筛选
    返回: JSON 格式的排行榜列表
    """
    import json

    category_filter = ""
    params = []
    if category_id is not None:
        category_filter = "AND b.category_id = %s"
        params.append(category_id)
    params.append(limit)

    sql = f"""
        SELECT b.id, b.title, b.author, b.price, b.stock,
               SUM(oi.quantity) AS sales_count,
               SUM(oi.quantity * oi.price) AS total_revenue
        FROM order_items oi
        JOIN books b ON oi.book_id = b.id
        JOIN orders o ON oi.order_id = o.id
        WHERE o.status IN ('paid', 'shipped', 'completed')
          {category_filter}
        GROUP BY oi.book_id
        ORDER BY sales_count DESC
        LIMIT %s
    """
    rows = _query(sql, params)
    if not rows:
        return "暂无销售数据"
    return json.dumps(rows, ensure_ascii=False, default=str)


@tool
def search_books(keyword: str = "", category_id: int = None) -> str:
    """
    搜索图书（可按书名关键词和分类筛选）。
    参数:
        keyword: 书名关键词，为空则返回全部
        category_id: 可选，按分类 ID 筛选
    返回: JSON 格式的图书列表（最多 20 条）
    """
    import json

    conditions = ["b.status = 1"]
    params = []

    if keyword:
        conditions.append("b.title LIKE %s")
        params.append(f"%{keyword}%")
    if category_id is not None:
        conditions.append("b.category_id = %s")
        params.append(category_id)

    where = " AND ".join(conditions)
    sql = f"""
        SELECT b.id, b.title, b.author, b.price, b.stock,
               c.name AS category_name, b.description
        FROM books b
        LEFT JOIN categories c ON b.category_id = c.id
        WHERE {where}
        ORDER BY b.created_at DESC
        LIMIT 20
    """
    rows = _query(sql, params)
    if not rows:
        return f"未找到与'{keyword}'相关的图书"
    return json.dumps(rows, ensure_ascii=False, default=str)


@tool
def get_book_detail(book_id: int) -> str:
    """
    查询某本书的详细信息，包括价格、库存、评分和评价数量。
    参数:
        book_id: 图书 ID
    返回: JSON 格式的图书详情
    """
    import json

    sql = """
        SELECT b.*, c.name AS category_name,
               COALESCE(ROUND(AVG(r.rating), 1), 0) AS avg_rating,
               COUNT(r.id) AS review_count
        FROM books b
        LEFT JOIN categories c ON b.category_id = c.id
        LEFT JOIN reviews r ON r.user_id IN (
            SELECT o.user_id FROM orders o
            JOIN order_items oi ON o.id = oi.order_id
            WHERE oi.book_id = b.id
        )
        WHERE b.id = %s
        GROUP BY b.id
    """
    rows = _query(sql, (book_id,))
    if not rows:
        return f"未找到 ID 为 {book_id} 的图书"
    return json.dumps(rows[0], ensure_ascii=False, default=str)


@tool
def get_category_stats() -> str:
    """
    查询各图书分类的销售统计（按销量从高到低排名）。
    无参数。
    返回: JSON 格式的分类销售统计列表
    """
    import json

    sql = """
        SELECT c.id, c.name,
               COUNT(DISTINCT o.id) AS order_count,
               COALESCE(SUM(oi.quantity), 0) AS total_sold,
               COALESCE(SUM(oi.quantity * oi.price), 0) AS total_revenue,
               COUNT(DISTINCT b.id) AS book_count
        FROM categories c
        LEFT JOIN books b ON b.category_id = c.id AND b.status = 1
        LEFT JOIN order_items oi ON oi.book_id = b.id
        LEFT JOIN orders o ON o.id = oi.order_id
            AND o.status IN ('paid', 'shipped', 'completed')
        GROUP BY c.id, c.name
        ORDER BY total_sold DESC
    """
    rows = _query(sql)
    if not rows:
        return "暂无销售数据"
    return json.dumps(rows, ensure_ascii=False, default=str)


@tool
def get_store_overview() -> str:
    """
    查询店铺今日概览（图书总数、今日订单数、今日营业额）。
    无参数。
    返回: JSON 格式的概览数据
    """
    import json
    from datetime import date

    today = date.today().strftime("%Y-%m-%d")

    # 图书总数
    book_count = _query("SELECT COUNT(*) AS cnt FROM books WHERE status = 1")[0]["cnt"]

    # 今日订单（排除 pending）
    order_sql = """
        SELECT COUNT(*) AS cnt, COALESCE(SUM(total_amount), 0) AS total
        FROM orders
        WHERE DATE(created_at) = %s AND status IN ('paid', 'shipped', 'completed')
    """
    order_row = _query(order_sql, (today,))[0]

    # 今日售出数量
    sold_sql = """
        SELECT COALESCE(SUM(oi.quantity), 0) AS cnt
        FROM order_items oi
        JOIN orders o ON o.id = oi.order_id
        WHERE DATE(o.created_at) = %s AND o.status IN ('paid', 'shipped', 'completed')
    """
    sold_count = _query(sold_sql, (today,))[0]["cnt"]

    return json.dumps({
        "date": today,
        "total_books": book_count,
        "today_orders": order_row["cnt"],
        "today_revenue": float(order_row["total"] or 0),
        "today_books_sold": sold_count,
    }, ensure_ascii=False)


# 所有工具列表
ALL_TOOLS = [
    get_bestsellers,
    search_books,
    get_book_detail,
    get_category_stats,
    get_store_overview,
]

# 工具名称到工具对象的映射（用于快速查找）
TOOL_MAP = {tool.name: tool for tool in ALL_TOOLS}
