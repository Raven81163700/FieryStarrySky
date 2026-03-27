# -*- coding: utf-8 -*-
"""
Aurora Online Game Server - 数据库层
职责: 初始化 SQLite schema、提供账号 CRUD 操作
"""

import sqlite3
import hashlib
import os
import logging

from config import DB_PATH

logger = logging.getLogger(__name__)


# ─────────────────────────────────────────────────
#  初始化
# ─────────────────────────────────────────────────

def init_db():
    """创建数据库目录和表结构（幂等操作）"""
    os.makedirs(os.path.dirname(DB_PATH), exist_ok=True)
    conn = _connect()
    try:
        c = conn.cursor()
        # 账号表
        c.execute("""
            CREATE TABLE IF NOT EXISTS accounts (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                username    TEXT    NOT NULL UNIQUE COLLATE NOCASE,
                pass_hash   TEXT    NOT NULL,
                created_at  TEXT    NOT NULL DEFAULT (datetime('now')),
                last_login  TEXT
            )
        """)
        # 角色表：包含资源路径与展示图路径
        c.execute("""
            CREATE TABLE IF NOT EXISTS characters (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                account_id      INTEGER NOT NULL REFERENCES accounts(id),
                name            TEXT    NOT NULL UNIQUE COLLATE NOCASE,
                gender          INTEGER NOT NULL DEFAULT 0,
                background_path TEXT,
                character_path  TEXT,
                preview_path    TEXT,
                level           INTEGER NOT NULL DEFAULT 1,
                exp             INTEGER NOT NULL DEFAULT 0,
                map_id          INTEGER NOT NULL DEFAULT 1,
                x               INTEGER NOT NULL DEFAULT 0,
                y               INTEGER NOT NULL DEFAULT 0,
                created_at      TEXT    NOT NULL DEFAULT (datetime('now'))
            )
        """)
        # 对于已有老表，尝试添加新列以兼容升级（忽略已存在的错误）
        try:
            c.execute("ALTER TABLE characters ADD COLUMN gender INTEGER NOT NULL DEFAULT 0")
        except Exception:
            pass
        for col in ("background_path", "character_path", "preview_path"):
            try:
                c.execute("ALTER TABLE characters ADD COLUMN %s TEXT" % col)
            except Exception:
                pass
        conn.commit()
        logger.info("数据库初始化完成: %s", DB_PATH)
    finally:
        conn.close()


# ─────────────────────────────────────────────────
#  内部工具
# ─────────────────────────────────────────────────

def _connect():
    """返回一个 sqlite3 连接（启用 WAL 模式提升并发读性能）"""
    conn = sqlite3.connect(DB_PATH, timeout=10, check_same_thread=False)
    conn.execute("PRAGMA journal_mode=WAL")
    conn.execute("PRAGMA foreign_keys=ON")
    conn.row_factory = sqlite3.Row
    return conn


def _hash_password(plain: str) -> str:
    """SHA-256 + 固定盐前缀（轻量方案，适合 JavaME 游戏场景）
    生产环境可替换为 bcrypt: pip install bcrypt
    """
    salted = "aurora_salt_v1:" + plain
    return hashlib.sha256(salted.encode("utf-8")).hexdigest()


def _verify_password(plain: str, stored_hash: str) -> bool:
    return _hash_password(plain) == stored_hash


# ─────────────────────────────────────────────────
#  账号操作
# ─────────────────────────────────────────────────

# 用户名规则
_USERNAME_MIN = 3
_USERNAME_MAX = 16
_PASSWORD_MIN = 6
_PASSWORD_MAX = 32
_ALLOWED_CHARS = set("abcdefghijklmnopqrstuvwxyz"
                     "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                     "0123456789_")


def _validate_username(username: str):
    """返回 None 表示合法，否则返回错误描述字符串"""
    if not (_USERNAME_MIN <= len(username) <= _USERNAME_MAX):
        return "用户名长度须在 {}-{} 之间".format(_USERNAME_MIN, _USERNAME_MAX)
    if not all(c in _ALLOWED_CHARS for c in username):
        return "用户名只能包含字母、数字和下划线"
    return None


def _validate_password(password: str):
    if not (_PASSWORD_MIN <= len(password) <= _PASSWORD_MAX):
        return "密码长度须在 {}-{} 之间".format(_PASSWORD_MIN, _PASSWORD_MAX)
    return None


def register(username: str, password: str):
    """
    注册账号。
    返回 (True,  account_id)   成功
    返回 (False, 错误描述)      失败
    """
    err = _validate_username(username)
    if err:
        return False, err
    err = _validate_password(password)
    if err:
        return False, err

    conn = _connect()
    try:
        conn.execute(
            "INSERT INTO accounts (username, pass_hash) VALUES (?, ?)",
            (username, _hash_password(password))
        )
        conn.commit()
        row = conn.execute(
            "SELECT id FROM accounts WHERE username = ?", (username,)
        ).fetchone()
        logger.info("新账号注册: %s (id=%d)", username, row["id"])
        return True, row["id"]
    except sqlite3.IntegrityError:
        return False, "用户名已存在"
    finally:
        conn.close()


def login(username: str, password: str):
    """
    验证登录。
    返回 (True,  account_id)   成功
    返回 (False, 错误描述)      失败
    """
    conn = _connect()
    try:
        row = conn.execute(
            "SELECT id, pass_hash FROM accounts WHERE username = ?",
            (username,)
        ).fetchone()

        if row is None:
            return False, "账号不存在"
        if not _verify_password(password, row["pass_hash"]):
            return False, "密码错误"

        # 更新最后登录时间
        conn.execute(
            "UPDATE accounts SET last_login = datetime('now') WHERE id = ?",
            (row["id"],)
        )
        conn.commit()
        logger.info("账号登录成功: %s (id=%d)", username, row["id"])
        return True, row["id"]
    finally:
        conn.close()


# ─────────────────────────────────────────────────
#  角色相关操作
# ─────────────────────────────────────────────────

def create_character(account_id: int, name: str, gender: int, background_path: str, character_path: str, preview_path: str = None):
    """
    创建角色并返回 (True, char_id) 或 (False, 错误描述)
    """
    conn = _connect()
    try:
        cur = conn.cursor()
        cur.execute(
            "INSERT INTO characters (account_id, name, gender, background_path, character_path, preview_path) VALUES (?, ?, ?, ?, ?, ?)",
            (account_id, name, gender, background_path, character_path, preview_path)
        )
        conn.commit()
        row = cur.execute("SELECT id FROM characters WHERE rowid = last_insert_rowid()").fetchone()
        return True, row[0]
    except sqlite3.IntegrityError:
        return False, "角色名已存在"
    finally:
        conn.close()


def list_characters(account_id: int):
    """返回角色 dict 列表"""
    conn = _connect()
    try:
        rows = conn.execute(
            "SELECT id, name, gender, background_path, character_path, preview_path, created_at FROM characters WHERE account_id = ? ORDER BY id",
            (account_id,)
        ).fetchall()
        out = []
        for r in rows:
            out.append({
                'id': r['id'], 'name': r['name'], 'gender': r['gender'],
                'background_path': r['background_path'] or '', 'character_path': r['character_path'] or '',
                'preview_path': r['preview_path'] or '', 'created_at': r['created_at']
            })
        return out
    finally:
        conn.close()


def delete_character(account_id: int, char_id: int) -> bool:
    conn = _connect()
    try:
        cur = conn.cursor()
        cur.execute("DELETE FROM characters WHERE account_id = ? AND id = ?", (account_id, char_id))
        conn.commit()
        return cur.rowcount > 0
    finally:
        conn.close()
