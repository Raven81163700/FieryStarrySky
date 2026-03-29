# -*- coding: utf-8 -*-
"""
Aurora Online Game Server - 配置
"""

# ── 网络 ──────────────────────────────────────────
HOST = "0.0.0.0"
PORT = 9000

# ── 数据库 ────────────────────────────────────────
import os
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH  = os.path.join(BASE_DIR, "data", "aurora.db")

# ── 星图配置 ──────────────────────────────────────
# 优先从该配置文件加载星图数据（systems/links/bodies/constellations/domains）。
# 可通过环境变量 AURORA_STAR_MAP_CONFIG 覆盖。
STAR_MAP_CONFIG_PATH = os.path.join(BASE_DIR, "data", "star_map_config.json")

# ── 协议 ──────────────────────────────────────────
ENCODING        = "utf-8"
MSG_SEPARATOR   = "\n"          # 消息结束符
FIELD_SEPARATOR = "|"           # 字段分隔符
MAX_MSG_LEN     = 131072        # 允许携带角色预览图 base64

# ── 安全 ──────────────────────────────────────────
BCRYPT_ROUNDS = 12              # bcrypt cost factor (生产环境建议 12)

# ── 日志 ──────────────────────────────────────────
LOG_PATH = os.path.join(BASE_DIR, "logs", "server.log")
LOG_LEVEL = "DEBUG"             # DEBUG / INFO / WARNING / ERROR
