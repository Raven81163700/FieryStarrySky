# -*- coding: utf-8 -*-
"""
Aurora Online Game Server - 主入口
每个客户端连接分配一个独立线程处理（适合 JavaME 游戏规模）。
"""

import socket
import threading
import logging
import os
import sys

import config
import db
import protocol as P
from session import Session, session_mgr
from handlers import dispatch

# ─────────────────────────────────────────────────
#  日志配置
# ─────────────────────────────────────────────────

def _setup_logging():
    os.makedirs(os.path.dirname(config.LOG_PATH), exist_ok=True)
    fmt = logging.Formatter(
        "[%(asctime)s] %(levelname)-8s %(name)s - %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S"
    )
    level = getattr(logging, config.LOG_LEVEL.upper(), logging.DEBUG)

    root = logging.getLogger()
    root.setLevel(level)

    # 控制台
    ch = logging.StreamHandler(sys.stdout)
    ch.setFormatter(fmt)
    root.addHandler(ch)

    # 文件
    fh = logging.FileHandler(config.LOG_PATH, encoding="utf-8")
    fh.setFormatter(fmt)
    root.addHandler(fh)


logger = logging.getLogger(__name__)


# ─────────────────────────────────────────────────
#  客户端连接处理
# ─────────────────────────────────────────────────

def _handle_client(conn, addr):
    """每个客户端连接的工作线程"""
    session = Session(conn, addr)
    session_mgr.add(session)

    buf = ""
    try:
        while True:
            try:
                chunk = conn.recv(1024)
            except OSError:
                break

            if not chunk:
                break   # 客户端断开

            buf += chunk.decode(config.ENCODING, errors="replace")

            # 按换行符切割完整消息，支持一次收到多条
            while config.MSG_SEPARATOR in buf:
                line, buf = buf.split(config.MSG_SEPARATOR, 1)
                line = line.strip()
                if not line:
                    continue
                try:
                    parts = P.parse(line)
                    dispatch(session, parts)
                except P.ProtocolError as e:
                    logger.warning("协议错误 %s: %s", addr, e)
                    session.send(P.make_err("?", str(e)))

    except Exception as e:
        logger.exception("连接异常 %s: %s", addr, e)
    finally:
        session_mgr.remove(session)
        session.close()
        logger.info("连接关闭: %s", addr)


# ─────────────────────────────────────────────────
#  TCP 服务器
# ─────────────────────────────────────────────────

def run_server():
    _setup_logging()
    db.init_db()

    server_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_sock.bind((config.HOST, config.PORT))
    server_sock.listen(64)

    logger.info("Aurora 游戏服务器启动  %s:%d", config.HOST, config.PORT)
    logger.info("等待客户端连接...")

    try:
        while True:
            conn, addr = server_sock.accept()
            t = threading.Thread(
                target=_handle_client,
                args=(conn, addr),
                daemon=True,
                name="client-{}:{}".format(*addr)
            )
            t.start()
    except KeyboardInterrupt:
        logger.info("服务器正在关闭...")
    finally:
        server_sock.close()


if __name__ == "__main__":
    run_server()
