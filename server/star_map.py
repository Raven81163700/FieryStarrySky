# -*- coding: utf-8 -*-
"""
Aurora Online Game Server - Star Map Data Loader

优先从 JSON 配置加载星图；当配置缺失或损坏时，自动回退到内置默认数据。
支持字段: systems / links / bodies / constellations / domains
"""

import json
import os

import config


# ─────────────────────────────────────────────────
#  内置默认数据（配置缺失时兜底）
# ─────────────────────────────────────────────────

_DEFAULT_SYSTEMS = [
    {"id": 1, "name": "Sol", "x": 120, "y": 120, "security": 3},
    {"id": 2, "name": "Alpha", "x": 300, "y": 90, "security": 2},
    {"id": 3, "name": "Cygnus", "x": 520, "y": 130, "security": 1},
    {"id": 4, "name": "Orion", "x": 260, "y": 280, "security": 2},
    {"id": 5, "name": "Perseus", "x": 480, "y": 300, "security": 1},
    {"id": 6, "name": "Vega", "x": 700, "y": 110, "security": 2},
    {"id": 7, "name": "Sirius", "x": 860, "y": 170, "security": 2},
    {"id": 8, "name": "Deneb", "x": 1040, "y": 120, "security": 1},
    {"id": 9, "name": "Rigel", "x": 760, "y": 320, "security": 1},
    {"id": 10, "name": "Antares", "x": 980, "y": 350, "security": 1},
    {"id": 11, "name": "Polaris", "x": 1220, "y": 150, "security": 3},
    {"id": 12, "name": "Arcturus", "x": 1320, "y": 300, "security": 2},
    {"id": 13, "name": "Altair", "x": 560, "y": 470, "security": 2},
    {"id": 14, "name": "Bellatrix", "x": 900, "y": 500, "security": 1},
]

_DEFAULT_LINKS = [
    {"from_id": 1, "to_id": 2, "link_type": 1, "cost": 5},
    {"from_id": 2, "to_id": 3, "link_type": 1, "cost": 6},
    {"from_id": 1, "to_id": 4, "link_type": 1, "cost": 4},
    {"from_id": 4, "to_id": 5, "link_type": 1, "cost": 5},
    {"from_id": 2, "to_id": 4, "link_type": 1, "cost": 3},
    {"from_id": 3, "to_id": 6, "link_type": 1, "cost": 4},
    {"from_id": 6, "to_id": 7, "link_type": 1, "cost": 3},
    {"from_id": 7, "to_id": 8, "link_type": 1, "cost": 3},
    {"from_id": 6, "to_id": 9, "link_type": 1, "cost": 4},
    {"from_id": 9, "to_id": 10, "link_type": 1, "cost": 4},
    {"from_id": 8, "to_id": 11, "link_type": 1, "cost": 5},
    {"from_id": 11, "to_id": 12, "link_type": 1, "cost": 4},
    {"from_id": 5, "to_id": 13, "link_type": 1, "cost": 4},
    {"from_id": 9, "to_id": 13, "link_type": 1, "cost": 5},
    {"from_id": 10, "to_id": 14, "link_type": 1, "cost": 3},
    {"from_id": 13, "to_id": 14, "link_type": 1, "cost": 5},
]

_DEFAULT_BODIES = [
    {"id": 1001, "system_id": 1, "body_type": 1, "name": "Sol-A", "x": 58, "y": 44, "primary": 0},
    {"id": 1002, "system_id": 1, "body_type": 2, "name": "Sol-I", "x": 28, "y": 58, "primary": 1001},
    {"id": 1003, "system_id": 1, "body_type": 4, "name": "Sol-Belt", "x": 64, "y": 70, "primary": 1001},
    {"id": 1004, "system_id": 1, "body_type": 3, "name": "Sol-Station", "x": 90, "y": 54, "primary": 1001},
]

_DEFAULT_CONSTELLATIONS = []
_DEFAULT_DOMAINS = []


_MAP = {
    "systems": _DEFAULT_SYSTEMS,
    "links": _DEFAULT_LINKS,
    "bodies": _DEFAULT_BODIES,
    "constellations": _DEFAULT_CONSTELLATIONS,
    "domains": _DEFAULT_DOMAINS,
}


def _to_int(v, default):
    try:
        return int(v)
    except Exception:
        return default


def _normalize_map(raw):
    systems_raw = raw.get("systems") if isinstance(raw.get("systems"), list) else []
    links_raw = raw.get("links") if isinstance(raw.get("links"), list) else []
    bodies_raw = raw.get("bodies") if isinstance(raw.get("bodies"), list) else []
    constellations_raw = raw.get("constellations") if isinstance(raw.get("constellations"), list) else []
    domains_raw = raw.get("domains") if isinstance(raw.get("domains"), list) else []

    systems = []
    for s in systems_raw:
        sid = _to_int(s.get("id"), 0)
        if sid <= 0:
            continue
        systems.append(
            {
                "id": sid,
                "name": str(s.get("name") or ("System-" + str(sid))),
                "x": _to_int(s.get("x"), 0),
                "y": _to_int(s.get("y"), 0),
                "security": _to_int(s.get("security"), 1),
            }
        )
    systems.sort(key=lambda x: x["id"])
    valid_ids = set([x["id"] for x in systems])

    links = []
    for l in links_raw:
        from_id = _to_int(l.get("from_id"), 0)
        to_id = _to_int(l.get("to_id"), 0)
        if from_id <= 0 or to_id <= 0 or from_id == to_id:
            continue
        if from_id not in valid_ids or to_id not in valid_ids:
            continue
        links.append(
            {
                "from_id": from_id,
                "to_id": to_id,
                "link_type": max(1, _to_int(l.get("link_type"), 1)),
                "cost": max(1, _to_int(l.get("cost"), 1)),
            }
        )
    links.sort(key=lambda x: (x["from_id"], x["to_id"], x["link_type"]))

    bodies = []
    for b in bodies_raw:
        bid = _to_int(b.get("id"), 0)
        sid = _to_int(b.get("system_id"), 0)
        if bid <= 0 or sid not in valid_ids:
            continue
        bodies.append(
            {
                "id": bid,
                "system_id": sid,
                "body_type": max(1, _to_int(b.get("body_type"), 1)),
                "name": str(b.get("name") or ""),
                "x": _to_int(b.get("x"), 0),
                "y": _to_int(b.get("y"), 0),
                "primary": _to_int(b.get("primary"), 0),
            }
        )
    bodies.sort(key=lambda x: x["id"])

    constellations = []
    valid_constellation_ids = set()
    for c in constellations_raw:
        cid = _to_int(c.get("id"), 0)
        if cid <= 0:
            continue
        system_ids = []
        for sid in c.get("systemIds", []):
            sid2 = _to_int(sid, 0)
            if sid2 in valid_ids:
                system_ids.append(sid2)
        constellations.append(
            {
                "id": cid,
                "name": str(c.get("name") or ("Constellation-" + str(cid))),
                "controller": str(c.get("controller") or ""),
                "description": str(c.get("description") or ""),
                "color": str(c.get("color") or "#ffb74d"),
                "systemIds": system_ids,
            }
        )
        valid_constellation_ids.add(cid)
    constellations.sort(key=lambda x: x["id"])

    domains = []
    for d in domains_raw:
        did = _to_int(d.get("id"), 0)
        if did <= 0:
            continue
        constellation_ids = []
        for cid in d.get("constellationIds", []):
            cid2 = _to_int(cid, 0)
            if cid2 in valid_constellation_ids:
                constellation_ids.append(cid2)
        domains.append(
            {
                "id": did,
                "name": str(d.get("name") or ("Domain-" + str(did))),
                "controller": str(d.get("controller") or ""),
                "description": str(d.get("description") or ""),
                "color": str(d.get("color") or "rgba(72,196,255,0.3)"),
                "constellationIds": constellation_ids,
            }
        )
    domains.sort(key=lambda x: x["id"])

    return {
        "systems": systems,
        "links": links,
        "bodies": bodies,
        "constellations": constellations,
        "domains": domains,
    }


def _resolve_map_config_path():
    path = os.environ.get("AURORA_STAR_MAP_CONFIG", "").strip()
    if path:
        return path
    return config.STAR_MAP_CONFIG_PATH


def reload_map():
    global _MAP
    path = _resolve_map_config_path()
    if os.path.isfile(path):
        try:
            with open(path, "r", encoding="utf-8") as f:
                raw = json.load(f)
            normalized = _normalize_map(raw if isinstance(raw, dict) else {})
            if len(normalized["systems"]) > 0:
                _MAP = normalized
                return
        except Exception:
            pass

    _MAP = {
        "systems": _DEFAULT_SYSTEMS,
        "links": _DEFAULT_LINKS,
        "bodies": _DEFAULT_BODIES,
        "constellations": _DEFAULT_CONSTELLATIONS,
        "domains": _DEFAULT_DOMAINS,
    }


reload_map()


def all_systems():
    return _MAP["systems"]


def all_links():
    return _MAP["links"]


def all_constellations():
    return _MAP["constellations"]


def all_domains():
    return _MAP["domains"]


def bodies_by_system(system_id):
    return [b for b in _MAP["bodies"] if b["system_id"] == system_id]


def system_exists(system_id):
    for s in _MAP["systems"]:
        if s["id"] == system_id:
            return True
    return False
