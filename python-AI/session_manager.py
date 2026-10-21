"""
会话管理器 - 内存存储 + 自动过期清理
"""

import uuid
import time
import threading


class SessionManager:
    """管理聊天会话的生命周期"""

    def __init__(self, timeout_seconds=1800, cleanup_interval=300):
        """
        参数:
            timeout_seconds: 会话过期时间（秒），默认 30 分钟
            cleanup_interval: 清理间隔（秒），默认 5 分钟
        """
        self._sessions: dict[str, dict] = {}
        self._timeout = timeout_seconds
        self._lock = threading.Lock()
        self._start_cleanup(cleanup_interval)

    def create_session(self, system_prompt: str) -> str:
        """创建新会话，返回 session_id"""
        session_id = str(uuid.uuid4())
        now = time.time()
        with self._lock:
            self._sessions[session_id] = {
                "messages": [{"role": "system", "content": system_prompt}],
                "created_at": now,
                "last_access": now,
            }
        return session_id

    def get_session(self, session_id: str) -> dict | None:
        """获取会话，不存在或已过期返回 None"""
        with self._lock:
            session = self._sessions.get(session_id)
            if session is None:
                return None
            if time.time() - session["last_access"] > self._timeout:
                del self._sessions[session_id]
                return None
            session["last_access"] = time.time()
            return session

    def add_message(self, session_id: str, role: str, content: str) -> bool:
        """向会话添加消息，返回是否成功"""
        session = self.get_session(session_id)
        if session is None:
            return False
        session["messages"].append({"role": role, "content": content})
        return True

    def get_messages(self, session_id: str) -> list[dict] | None:
        """获取会话消息列表，会话不存在返回 None"""
        session = self.get_session(session_id)
        if session is None:
            return None
        return session["messages"]

    def cleanup_expired(self):
        """清理所有过期会话"""
        now = time.time()
        with self._lock:
            expired = [
                sid for sid, s in self._sessions.items()
                if now - s["last_access"] > self._timeout
            ]
            for sid in expired:
                del self._sessions[sid]

    def _start_cleanup(self, interval: int):
        """启动后台定时清理线程"""
        def _cleanup_loop():
            while True:
                time.sleep(interval)
                self.cleanup_expired()

        thread = threading.Thread(target=_cleanup_loop, daemon=True)
        thread.start()

    @property
    def active_count(self) -> int:
        """当前活跃会话数"""
        with self._lock:
            return len(self._sessions)
