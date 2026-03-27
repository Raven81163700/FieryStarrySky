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
import star_map
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


def _resolve_background_path(background_id: int):
    if background_id >= 1 and background_id <= 6:
        return "/game_res/background/background_%d.png" % background_id
    return None


def _resolve_character_path(character_id: int):
    if character_id >= 1 and character_id <= 3:
        return "/game_res/character/male/male%d.png" % character_id
    if character_id >= 101 and character_id <= 104:
        return "/game_res/character/female/female%d.png" % (character_id - 100)
    if character_id >= 201 and character_id <= 202:
        return "/game_res/character/no_sex/normal%d.png" % (character_id - 200)
    return None


def _resolve_background_id_from_path(background_path: str):
    if not background_path:
        return None
    p = background_path.strip()
    marker = "/game_res/background/background_"
    if p.startswith(marker) and p.endswith(".png"):
        num = p[len(marker):-4]
        try:
            bg_id = int(num)
            if bg_id >= 1 and bg_id <= 6:
                return bg_id
        except Exception:
            return None
    return None


def _resolve_character_id_from_path(character_path: str):
    if not character_path:
        return None
    p = character_path.strip()
    if p.startswith("/game_res/character/male/male") and p.endswith(".png"):
        num = p[len("/game_res/character/male/male"):-4]
        try:
            cid = int(num)
            if cid >= 1 and cid <= 3:
                return cid
        except Exception:
            return None
    if p.startswith("/game_res/character/female/female") and p.endswith(".png"):
        num = p[len("/game_res/character/female/female"):-4]
        try:
            cid = int(num)
            if cid >= 1 and cid <= 4:
                return 100 + cid
        except Exception:
            return None
    if p.startswith("/game_res/character/no_sex/normal") and p.endswith(".png"):
        num = p[len("/game_res/character/no_sex/normal"):-4]
        try:
            cid = int(num)
            if cid >= 1 and cid <= 2:
                return 200 + cid
        except Exception:
            return None
    return None


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
        session.send(P.make_err(cmd, result))
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


def handle_list_chars(session, parts):
    """
    客户端: LIST_CHARS
    返回: OK|LIST_CHARS|N|id|name|gender|background|character|preview|...
    """
    cmd = P.CMD_LIST_CHARS
    # accept either: LIST_CHARS|account_id  or LIST_CHARS (requires logged-in session)
    account_id = None
    if len(parts) >= 2:
        try:
            account_id = int(parts[1])
        except Exception:
            account_id = None
    if account_id is None:
        if not session.authed:
            session.send(P.make_err(cmd, "请先登录或传入 account_id"))
            return
        account_id = session.account_id

    chars = db.list_characters(account_id)

    # attach preview_base64 from stored preview_path when available
    out = []
    import base64
    import os
    cfg = __import__('config')
    for c in chars:
        pc = dict(c)
        preview_b64 = ''
        preview_path = c.get('preview_path', '')
        # Return ID in LIST_CHARS fields for background/character, keep protocol shape unchanged.
        bg_id = _resolve_background_id_from_path(c.get('background_path', ''))
        ch_id = _resolve_character_id_from_path(c.get('character_path', ''))
        pc['background_path'] = '' if bg_id is None else str(bg_id)
        pc['character_path'] = '' if ch_id is None else str(ch_id)
        if preview_path:
            fp = os.path.join(cfg.BASE_DIR, preview_path)
            if os.path.isfile(fp):
                try:
                    with open(fp, 'rb') as f:
                        preview_b64 = base64.b64encode(f.read()).decode('ascii')
                except Exception:
                    preview_b64 = ''
        pc['preview_base64'] = preview_b64
        out.append(pc)

    session.send(P.make_list_chars(out))


def handle_create_char(session, parts):
    """
    客户端: CREATE_CHAR|name|gender|background_id|character_id
    成功: OK|CREATE_CHAR|char_id
    失败: ERR|CREATE_CHAR|原因
    """
    cmd = P.CMD_CREATE_CHAR
    # support two formats:
    #  CREATE_CHAR|account_id|name|gender|background|character[|preview_base64]
    #  CREATE_CHAR|name|gender|background|character[|preview_base64]  (requires logged-in session)
    if len(parts) < 5:
        session.send(P.make_err(cmd, "参数不足"))
        return

    account_id = None
    name = None
    gender = 0
    background = None
    character = None

    # detect if parts[1] is account id
    try:
        maybe = int(parts[1])
        # format with account id: CREATE_CHAR|account_id|name|gender|background_id|character_id
        if len(parts) < 6:
            session.send(P.make_err(cmd, "参数不足"))
            return
        account_id = maybe
        name = parts[2].strip()
        try:
            gender = int(parts[3])
        except Exception:
            gender = 0
        bg_token = parts[4].strip()
        ch_token = parts[5].strip()
        try:
            background = _resolve_background_path(int(bg_token))
            character = _resolve_character_path(int(ch_token))
        except Exception:
            # backward compatibility for older clients that send full resource paths
            background = bg_token
            character = ch_token
    except Exception:
        # no account id in params, require logged-in session
        if not session.authed:
            session.send(P.make_err(cmd, "请先登录或传入 account_id"))
            return
        account_id = session.account_id
        name = parts[1].strip()
        try:
            gender = int(parts[2])
        except Exception:
            gender = 0
        bg_token = parts[3].strip()
        ch_token = parts[4].strip()
        try:
            background = _resolve_background_path(int(bg_token))
            character = _resolve_character_path(int(ch_token))
        except Exception:
            # backward compatibility for older clients that send full resource paths
            background = bg_token
            character = ch_token

    if not background or not character:
        session.send(P.make_err(cmd, "无效的背景ID或人物ID"))
        return

    # 先创建记录拿到 id，再保存服务端合成图并更新 preview_path
    preview_path = None
    ok, res = db.create_character(account_id, name, gender, background, character, preview_path)
    if not ok:
        session.send(P.make_err(cmd, res))
        return
    char_id = res

    # Prefer composing preview from background + character resources so server-side preview is always merged.
    composed_saved = False
    try:
        import os
        from PIL import Image
        cfg = __import__('config')

        # Prefer server-local copied assets in server/game_res.
        bg_fs = os.path.normpath(os.path.join(cfg.BASE_DIR, background.lstrip('/')))
        ch_fs = os.path.normpath(os.path.join(cfg.BASE_DIR, character.lstrip('/')))
        if not os.path.isfile(bg_fs):
            bg_fs = os.path.normpath(os.path.join(cfg.BASE_DIR, '..', background.lstrip('/')))
        if not os.path.isfile(ch_fs):
            ch_fs = os.path.normpath(os.path.join(cfg.BASE_DIR, '..', character.lstrip('/')))
        if os.path.isfile(bg_fs) and os.path.isfile(ch_fs):
            bg = Image.open(bg_fs).convert('RGBA').resize((64, 128))
            ch = Image.open(ch_fs).convert('RGBA').resize((64, 128))
            bg.paste(ch, (0, 0), ch)

            save_dir = os.path.join(cfg.BASE_DIR, 'data', 'characters', str(account_id))
            os.makedirs(save_dir, exist_ok=True)
            fn = os.path.join(save_dir, str(char_id) + '.png')
            bg.save(fn, format='PNG')

            preview_path = os.path.relpath(fn, cfg.BASE_DIR)
            conn = db._connect()
            try:
                conn.execute("UPDATE characters SET preview_path = ? WHERE id = ?", (preview_path, char_id))
                conn.commit()
            finally:
                conn.close()
            composed_saved = True
    except Exception:
        logger.exception('合成并保存预览图失败')

    if not composed_saved:
        logger.warning("角色已创建但预览图合成失败: account_id=%s, char_id=%s", account_id, char_id)

    session.send(P.make_ok(cmd, char_id))


def handle_get_star_map(session, parts):
    """
    客户端: GET_STAR_MAP
    返回: OK|GET_STAR_MAP|S|id|name|x|y|security|...|L|from|to|type|cost|...
    """
    cmd = P.CMD_GET_STAR_MAP
    systems = star_map.all_systems()
    links = star_map.all_links()

    extra = [str(len(systems))]
    for s in systems:
        extra += [str(s['id']), s['name'], str(s['x']), str(s['y']), str(s['security'])]
    extra += [str(len(links))]
    for l in links:
        extra += [str(l['from_id']), str(l['to_id']), str(l['link_type']), str(l['cost'])]

    session.send(P.make_ok(cmd, *extra))


def handle_get_system_bodies(session, parts):
    """
    客户端: GET_SYSTEM_BODIES|system_id
    返回: OK|GET_SYSTEM_BODIES|system_id|N|body_id|type|name|x|y|primary|...
    """
    cmd = P.CMD_GET_SYSTEM_BODIES
    if not _require_fields(session, cmd, parts, 2):
        return

    try:
        system_id = int(parts[1])
    except Exception:
        session.send(P.make_err(cmd, "无效的系统ID"))
        return

    if not star_map.system_exists(system_id):
        session.send(P.make_err(cmd, "系统不存在"))
        return

    bodies = star_map.bodies_by_system(system_id)
    extra = [str(system_id), str(len(bodies))]
    for b in bodies:
        extra += [
            str(b['id']), str(b['body_type']), b['name'],
            str(b['x']), str(b['y']), str(b['primary'])
        ]
    session.send(P.make_ok(cmd, *extra))


def handle_delete_char(session, parts):
    """
    客户端: DELETE_CHAR|char_id
    成功: OK|DELETE_CHAR|char_id
    失败: ERR|DELETE_CHAR|原因
    """
    cmd = P.CMD_DELETE_CHAR
    # support: DELETE_CHAR|account_id|char_id  or DELETE_CHAR|char_id (requires logged-in session)
    if len(parts) < 2:
        session.send(P.make_err(cmd, "参数不足"))
        return
    account_id = None
    char_id = None
    try:
        # try parse parts[1] as account id
        maybe = int(parts[1])
        if len(parts) >= 3:
            account_id = maybe
            try:
                char_id = int(parts[2])
            except Exception:
                session.send(P.make_err(cmd, "无效的角色 ID"))
                return
        else:
            # single numeric param -> treat as char_id if logged-in
            if session.authed:
                char_id = maybe
                account_id = session.account_id
            else:
                session.send(P.make_err(cmd, "请先登录或传入 account_id"))
                return
    except Exception:
        # parts[1] not numeric
        if not session.authed:
            session.send(P.make_err(cmd, "请先登录或传入 account_id"))
            return
        account_id = session.account_id
        try:
            char_id = int(parts[1])
        except Exception:
            session.send(P.make_err(cmd, "无效的角色 ID"))
            return
    ok = db.delete_character(account_id, char_id)
    if ok:
        session.send(P.make_ok(cmd, char_id))
    else:
        session.send(P.make_err(cmd, "未找到角色或无权限"))


# ─────────────────────────────────────────────────
#  分发表
# ─────────────────────────────────────────────────

_DISPATCH = {
    P.CMD_REGISTER: handle_register,
    P.CMD_LOGIN:    handle_login,
    P.CMD_PING:     handle_ping,
    P.CMD_LIST_CHARS: handle_list_chars,
    P.CMD_CREATE_CHAR: handle_create_char,
    P.CMD_DELETE_CHAR: handle_delete_char,
    P.CMD_GET_STAR_MAP: handle_get_star_map,
    P.CMD_GET_SYSTEM_BODIES: handle_get_system_bodies,
}

# 不需要登录就能调用的命令白名单
_NO_AUTH_REQUIRED = {P.CMD_REGISTER, P.CMD_LOGIN, P.CMD_PING, P.CMD_LIST_CHARS, P.CMD_CREATE_CHAR, P.CMD_DELETE_CHAR}


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
