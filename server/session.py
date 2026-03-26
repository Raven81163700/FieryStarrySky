# -*- coding: utf-8 -*-
"""
Aurora Online Game Server - 在线会话管理
每个已登录的 TCP 连接对应一个 Session 对象，统一由 SessionManager 管理。
"""

import threading
import logging

logger = logging.getLogger(__name__)


class Session:
    """代表一条已建立的客户端连接（可能已登录，也可能尚未登录）"""

    def __init__(self, conn, addr):
        self.conn       = conn          # socket 连接
        self.addr       = addr          # (ip, port)
        self.account_id = None          # 登录后才有值
        self.username   = None
        self.authed     = False         # 是否已通过登录验证

    def send(self, data: bytes):
        """线程安全地向客户端发送数据"""
        try:
            self.conn.sendall(data)
        except OSError as e:
            logger.warning("发送失败 %s: %s", self.addr, e)

    def close(self):
        try:
            self.conn.close()
        except OSError:
            pass

    def __repr__(self):
        if self.authed:
            return "<Session {} user={}>".format(self.addr, self.username)
        return "<Session {} unauthed>".format(self.addr)


class SessionManager:
    """
    全局会话表，线程安全。
    key: socket 对象
    """

    def __init__(self):
        self._lock     = threading.Lock()
        self._sessions = {}             # socket -> Session
        self._by_uid   = {}             # account_id -> Session（已登录）

    # ── 生命周期 ──────────────────────────────────

    def add(self, session: Session):
        with self._lock:
            self._sessions[session.conn] = session
        logger.debug("连接接入: %s  在线: %d", session.addr, self.count())

    def remove(self, session: Session):
        with self._lock:
            self._sessions.pop(session.conn, None)
            if session.account_id:
                self._by_uid.pop(session.account_id, None)
        logger.debug("连接断开: %s  在线: %d", session.addr, self.count())

    # ── 登录标记 ──────────────────────────────────

    def mark_logged_in(self, session: Session, account_id: int, username: str):
        """将 session 标记为已登录，并踢出同账号的旧连接"""
        with self._lock:
            # 踢出同账号已有连接（单点登录）
            old = self._by_uid.get(account_id)
            if old and old is not session:
                logger.info("踢出旧连接: %s (重复登录)", old.addr)
                old.send(b"ERR|LOGIN|account logged in elsewhere\n")
                old.close()
                self._sessions.pop(old.conn, None)

            session.account_id = account_id
            session.username   = username
            session.authed     = True
            self._by_uid[account_id] = session

    # ── 查询 ──────────────────────────────────────

    def get_by_uid(self, account_id: int):
        with self._lock:
            return self._by_uid.get(account_id)

    def count(self) -> int:
        with self._lock:
            return len(self._sessions)

    def online_users(self):
        """返回所有已登录用户名列表"""
        with self._lock:
            return [s.username for s in self._by_uid.values()]


# 全局单例
session_mgr = SessionManager()
