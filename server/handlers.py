# -*- coding: utf-8 -*-
"""
Aurora Online Game Server - 消息处理器
每个处理函数签名: handle_xxx(session, parts) -> None
  session : Session 对象
  parts   : parse() 返回的字段列表，parts[0] 是消息类型
"""

import logging

import db
import protocol as P
from session import session_mgr

logger = logging.getLogger(__name__)


# ─────────────────────────────────────────────────
#  工具
# ─────────────────────────────────────────────────

def _require_fields(session, cmd: str, parts: list, count: int) -> bool:
    """检查字段数量，不足则回复 ERR 并返回 False"""
    if len(parts) < count:
        session.send(P.make_err(cmd, "参数不足"))
        return False
    return True


def _require_auth(session, cmd: str) -> bool:
    """检查是否已登录，未登录则回复 ERR 并返回 False"""
    if not session.authed:
        session.send(P.make_err(cmd, "请先登录"))
        return False
    return True


# ─────────────────────────────────────────────────
#  处理器实现
# ─────────────────────────────────────────────────

def handle_register(session, parts):
    """
    客户端: REGISTER|username|password
    成功:   OK|REGISTER|account_id
    失败:   ERR|REGISTER|原因
    """
    cmd = P.CMD_REGISTER
    if not _require_fields(session, cmd, parts, 3):
        return

    username = parts[1].strip()
    password = parts[2].strip()

    ok, result = db.register(username, password)
    if ok:
        session.send(P.make_ok(cmd, result))   # result = account_id
        logger.info("注册成功: %s from %s", username, session.addr)
    else:
        session.send(P.make_err(cmd, result))  # result = 错误描述
        logger.info("注册失败: %s from %s — %s", username, session.addr, result)


def handle_login(session, parts):
    """
    客户端: LOGIN|username|password
    成功:   OK|LOGIN|account_id|username
    失败:   ERR|LOGIN|原因
    """
    cmd = P.CMD_LOGIN
    if not _require_fields(session, cmd, parts, 3):
        return

    username = parts[1].strip()
    password = parts[2].strip()

    ok, result = db.login(username, password)
    if ok:
        account_id = result
        session_mgr.mark_logged_in(session, account_id, username)
        session.send(P.make_ok(cmd, account_id, username))
        logger.info("登录成功: %s (id=%d) from %s", username, account_id, session.addr)
    else:
        session.send(P.make_err(cmd, result))
        logger.info("登录失败: %s from %s — %s", username, session.addr, result)


def handle_ping(session, parts):
    """客户端: PING  →  服务端: PING_ACK"""
    session.send(P.make_ping_ack())


# ─────────────────────────────────────────────────
#  分发表
# ─────────────────────────────────────────────────

_DISPATCH = {
    P.CMD_REGISTER: handle_register,
    P.CMD_LOGIN:    handle_login,
    P.CMD_PING:     handle_ping,
}

# 不需要登录就能调用的命令白名单
_NO_AUTH_REQUIRED = {P.CMD_REGISTER, P.CMD_LOGIN, P.CMD_PING}


def dispatch(session, parts):
    """
    根据消息类型分发到对应处理函数。
    对白名单以外的命令自动检查登录状态。
    """
    cmd = parts[0].upper()
    handler = _DISPATCH.get(cmd)
    if handler is None:
        session.send(P.make_err(cmd, "未知命令"))
        return

    if cmd not in _NO_AUTH_REQUIRED:
        if not _require_auth(session, cmd):
            return

    try:
        handler(session, parts)
    except Exception as e:
        logger.exception("处理命令 %s 时出错", cmd)
        session.send(P.make_err(cmd, "服务器内部错误"))
