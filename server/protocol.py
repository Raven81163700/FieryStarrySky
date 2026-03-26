# -*- coding: utf-8 -*-
"""
Aurora Online Game Server - 文本协议定义
格式: TYPE|field1|field2|...\n

客户端 → 服务端 (请求):
  REGISTER|username|password
  LOGIN|username|password
  PING

服务端 → 客户端 (响应):
  OK|cmd|...附加数据
  ERR|cmd|原因描述
  PING_ACK
"""

from config import FIELD_SEPARATOR, MSG_SEPARATOR, ENCODING, MAX_MSG_LEN

# ── 消息类型常量 ───────────────────────────────────

# 客户端发来
CMD_REGISTER = "REGISTER"
CMD_LOGIN    = "LOGIN"
CMD_PING     = "PING"

# 服务端回复
CMD_OK       = "OK"
CMD_ERR      = "ERR"
CMD_PING_ACK = "PING_ACK"


# ── 解包 ──────────────────────────────────────────

class ProtocolError(Exception):
    """协议格式错误"""
    pass


def parse(raw: str):
    """
    解析一条完整消息（已去掉末尾 \n）。
    返回 list[str]，第 0 项为消息类型。
    """
    raw = raw.strip()
    if not raw:
        raise ProtocolError("空消息")
    if len(raw.encode(ENCODING)) > MAX_MSG_LEN:
        raise ProtocolError("消息超长")
    parts = raw.split(FIELD_SEPARATOR)
    if not parts[0]:
        raise ProtocolError("消息类型为空")
    return parts


# ── 封包 ──────────────────────────────────────────

def make_ok(cmd: str, *extra) -> bytes:
    """构造成功响应帧"""
    parts = [CMD_OK, cmd] + [str(e) for e in extra]
    return (FIELD_SEPARATOR.join(parts) + MSG_SEPARATOR).encode(ENCODING)


def make_err(cmd: str, reason: str) -> bytes:
    """构造错误响应帧"""
    # reason 中不允许出现分隔符，替换掉
    reason = reason.replace(FIELD_SEPARATOR, " ")
    msg = FIELD_SEPARATOR.join([CMD_ERR, cmd, reason]) + MSG_SEPARATOR
    return msg.encode(ENCODING)


def make_ping_ack() -> bytes:
    return (CMD_PING_ACK + MSG_SEPARATOR).encode(ENCODING)
