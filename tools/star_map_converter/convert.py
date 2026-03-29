#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Convert star_map_editor JSON into server/client friendly data artifacts.
"""

from __future__ import annotations

import argparse
import json
import os
from pathlib import Path
from typing import Any, Dict, List


def clamp(v: float, lo: float, hi: float) -> float:
    return max(lo, min(hi, v))


def map_security(value: Any) -> int:
    """
    Convert editor security to current server/client integer levels (1..3).

    - float [-1, 1] -> 1..3 bands
    - integer 1..3 stays as is
    """
    try:
        v = float(value)
    except Exception:
        return 2

    # if already integer levels
    if v in (1, 2, 3):
        return int(v)

    v = clamp(v, -1.0, 1.0)
    if v >= 0.67:
        return 3
    if v >= 0.0:
        return 2
    return 1


def to_int(value: Any, default: int = 0) -> int:
    try:
        return int(round(float(value)))
    except Exception:
        return default


def normalize_system(raw: Dict[str, Any]) -> Dict[str, Any]:
    sid = to_int(raw.get("id"), 0)
    return {
        "id": sid,
        "name": str(raw.get("name") or f"System-{sid}"),
        "x": to_int(raw.get("x"), 0),
        "y": to_int(raw.get("y"), 0),
        "security": map_security(raw.get("security", 0)),
    }


def normalize_link(raw: Dict[str, Any]) -> Dict[str, Any]:
    out = {
        "from_id": to_int(raw.get("from_id"), 0),
        "to_id": to_int(raw.get("to_id"), 0),
        "link_type": to_int(raw.get("link_type"), 1),
        "cost": to_int(raw.get("cost"), 1),
    }
    if out["link_type"] <= 0:
        out["link_type"] = 1
    if out["cost"] <= 0:
        out["cost"] = 1
    return out


def normalize_body(raw: Dict[str, Any]) -> Dict[str, Any]:
    return {
        "id": to_int(raw.get("id"), 0),
        "system_id": to_int(raw.get("system_id"), 0),
        "body_type": to_int(raw.get("body_type"), 1),
        "name": str(raw.get("name") or ""),
        "x": to_int(raw.get("x"), 0),
        "y": to_int(raw.get("y"), 0),
        "primary": to_int(raw.get("primary"), 0),
    }


def normalize_constellation(raw: Dict[str, Any]) -> Dict[str, Any]:
    cid = to_int(raw.get("id"), 0)
    system_ids = raw.get("systemIds") if isinstance(raw.get("systemIds"), list) else []
    return {
        "id": cid,
        "name": str(raw.get("name") or f"Constellation-{cid}"),
        "controller": str(raw.get("controller") or ""),
        "description": str(raw.get("description") or ""),
        "color": str(raw.get("color") or "#ffb74d"),
        "systemIds": [to_int(x, 0) for x in system_ids if to_int(x, 0) > 0],
    }


def normalize_domain(raw: Dict[str, Any]) -> Dict[str, Any]:
    did = to_int(raw.get("id"), 0)
    constellation_ids = raw.get("constellationIds") if isinstance(raw.get("constellationIds"), list) else []
    return {
        "id": did,
        "name": str(raw.get("name") or f"Domain-{did}"),
        "controller": str(raw.get("controller") or ""),
        "description": str(raw.get("description") or ""),
        "color": str(raw.get("color") or "rgba(72,196,255,0.3)"),
        "constellationIds": [to_int(x, 0) for x in constellation_ids if to_int(x, 0) > 0],
    }


def normalize_map(src: Dict[str, Any]) -> Dict[str, List[Dict[str, Any]]]:
    systems_raw = src.get("systems") if isinstance(src.get("systems"), list) else []
    links_raw = src.get("links") if isinstance(src.get("links"), list) else []
    bodies_raw = src.get("bodies") if isinstance(src.get("bodies"), list) else []
    constellations_raw = src.get("constellations") if isinstance(src.get("constellations"), list) else []
    domains_raw = src.get("domains") if isinstance(src.get("domains"), list) else []

    systems = [normalize_system(s) for s in systems_raw]
    systems = [s for s in systems if s["id"] > 0]
    systems.sort(key=lambda x: x["id"])

    valid_ids = {s["id"] for s in systems}
    links = [normalize_link(l) for l in links_raw]
    links = [
        l for l in links
        if l["from_id"] > 0 and l["to_id"] > 0 and l["from_id"] != l["to_id"]
        and l["from_id"] in valid_ids and l["to_id"] in valid_ids
    ]
    links.sort(key=lambda x: (x["from_id"], x["to_id"], x["link_type"]))

    bodies = [normalize_body(b) for b in bodies_raw]
    bodies = [b for b in bodies if b["id"] > 0 and b["system_id"] in valid_ids]
    bodies.sort(key=lambda x: x["id"])

    constellations = [normalize_constellation(c) for c in constellations_raw]
    constellations = [c for c in constellations if c["id"] > 0]
    for c in constellations:
        c["systemIds"] = [sid for sid in c["systemIds"] if sid in valid_ids]
    constellations.sort(key=lambda x: x["id"])

    valid_constellation_ids = {c["id"] for c in constellations}
    domains = [normalize_domain(d) for d in domains_raw]
    domains = [d for d in domains if d["id"] > 0]
    for d in domains:
        d["constellationIds"] = [cid for cid in d["constellationIds"] if cid in valid_constellation_ids]
    domains.sort(key=lambda x: x["id"])

    return {
        "systems": systems,
        "links": links,
        "bodies": bodies,
        "constellations": constellations,
        "domains": domains,
    }


def py_repr(obj: Any) -> str:
    return json.dumps(obj, ensure_ascii=False, indent=4)


def make_server_py(normalized: Dict[str, List[Dict[str, Any]]]) -> str:
    systems = py_repr(normalized["systems"])
    links = py_repr(normalized["links"])
    bodies = py_repr(normalized["bodies"])
    constellations = py_repr(normalized["constellations"])
    domains = py_repr(normalized["domains"])
    return f'''# -*- coding: utf-8 -*-
"""
Generated by tools/star_map_converter/convert.py
Do not edit manually.
"""

SYSTEMS = {systems}

LINKS = {links}

BODIES = {bodies}

CONSTELLATIONS = {constellations}

DOMAINS = {domains}


def all_systems():
    return SYSTEMS


def all_links():
    return LINKS


def all_constellations():
    return CONSTELLATIONS


def all_domains():
    return DOMAINS


def bodies_by_system(system_id):
    return [b for b in BODIES if b.get("system_id") == system_id]


def system_exists(system_id):
    for s in SYSTEMS:
        if s.get("id") == system_id:
            return True
    return False
'''


def java_escape(s: str) -> str:
    out = []
    for ch in s:
        code = ord(ch)
        if ch == "\\":
            out.append("\\\\")
        elif ch == '"':
            out.append('\\"')
        elif 32 <= code <= 126:
            out.append(ch)
        else:
            out.append("\\u%04x" % code)
    return "".join(out)


def make_client_java(normalized: Dict[str, List[Dict[str, Any]]]) -> str:
    sys_lines = []
    for s in normalized["systems"]:
        sys_lines.append(
            f'        makeSystem({s["id"]}, "{java_escape(s["name"])}", {s["x"]}, {s["y"]}, {s["security"]})'
        )

    link_lines = []
    for l in normalized["links"]:
        link_lines.append(
            f'        makeLink({l["from_id"]}, {l["to_id"]}, {l["link_type"]}, {l["cost"]})'
        )

    body_lines = []
    for b in normalized["bodies"]:
        body_lines.append(
            f'        makeBody({b["id"]}, {b["system_id"]}, {b["body_type"]}, "{java_escape(b["name"])}", {b["x"]}, {b["y"]}, {b["primary"]})'
        )

    constellation_lines = []
    for c in normalized["constellations"]:
        sid_values = c.get("systemIds", [])
        sid_expr = "new int[] {" + ", ".join([str(x) for x in sid_values]) + "}"
        constellation_lines.append(
            '        makeConstellation({0}, "{1}", "{2}", "{3}", "{4}", {5})'.format(
                c["id"],
                java_escape(c["name"]),
                java_escape(c.get("controller", "")),
                java_escape(c.get("description", "")),
                java_escape(c.get("color", "")),
                sid_expr,
            )
        )

    domain_lines = []
    for d in normalized["domains"]:
        cid_values = d.get("constellationIds", [])
        cid_expr = "new int[] {" + ", ".join([str(x) for x in cid_values]) + "}"
        domain_lines.append(
            '        makeDomain({0}, "{1}", "{2}", "{3}", "{4}", {5})'.format(
                d["id"],
                java_escape(d["name"]),
                java_escape(d.get("controller", "")),
                java_escape(d.get("description", "")),
                java_escape(d.get("color", "")),
                cid_expr,
            )
        )

    systems_block = ",\n".join(sys_lines)
    links_block = ",\n".join(link_lines)
    bodies_block = ",\n".join(body_lines)
    constellations_block = ",\n".join(constellation_lines)
    domains_block = ",\n".join(domain_lines)

    return f'''package com.aurora.world;

public final class StarMapCatalogGenerated {{

    public static final class StarSystem {{
        public int id;
        public String name;
        public int x;
        public int y;
        public int security;
    }}

    public static final class SystemLink {{
        public int fromId;
        public int toId;
        public int linkType;
        public int cost;
    }}

    public static final class Body {{
        public int id;
        public int systemId;
        public int bodyType;
        public String name;
        public int x;
        public int y;
        public int primary;
    }}

    public static final class Constellation {{
        public int id;
        public String name;
        public String controller;
        public String description;
        public String color;
        public int[] systemIds;
    }}

    public static final class Domain {{
        public int id;
        public String name;
        public String controller;
        public String description;
        public String color;
        public int[] constellationIds;
    }}

    private static final StarSystem[] SYSTEMS = new StarSystem[] {{
{systems_block}
    }};

    private static final SystemLink[] LINKS = new SystemLink[] {{
{links_block}
    }};

    private static final Body[] BODIES = new Body[] {{
{bodies_block}
    }};

    private static final Constellation[] CONSTELLATIONS = new Constellation[] {{
{constellations_block}
    }};

    private static final Domain[] DOMAINS = new Domain[] {{
{domains_block}
    }};

    private StarMapCatalogGenerated() {{}}

    public static StarSystem[] systems() {{
        return SYSTEMS;
    }}

    public static SystemLink[] links() {{
        return LINKS;
    }}

    public static Body[] bodiesBySystem(int systemId) {{
        int count = 0;
        int i;
        for (i = 0; i < BODIES.length; i++) {{
            if (BODIES[i].systemId == systemId) count++;
        }}
        Body[] out = new Body[count];
        int p = 0;
        for (i = 0; i < BODIES.length; i++) {{
            if (BODIES[i].systemId == systemId) out[p++] = BODIES[i];
        }}
        return out;
    }}

    public static Constellation[] constellations() {{
        return CONSTELLATIONS;
    }}

    public static Domain[] domains() {{
        return DOMAINS;
    }}

    private static StarSystem makeSystem(int id, String name, int x, int y, int security) {{
        StarSystem s = new StarSystem();
        s.id = id;
        s.name = name;
        s.x = x;
        s.y = y;
        s.security = security;
        return s;
    }}

    private static SystemLink makeLink(int fromId, int toId, int linkType, int cost) {{
        SystemLink l = new SystemLink();
        l.fromId = fromId;
        l.toId = toId;
        l.linkType = linkType;
        l.cost = cost;
        return l;
    }}

    private static Body makeBody(int id, int systemId, int bodyType, String name, int x, int y, int primary) {{
        Body b = new Body();
        b.id = id;
        b.systemId = systemId;
        b.bodyType = bodyType;
        b.name = name;
        b.x = x;
        b.y = y;
        b.primary = primary;
        return b;
    }}

    private static Constellation makeConstellation(int id, String name, String controller, String description, String color, int[] systemIds) {{
        Constellation c = new Constellation();
        c.id = id;
        c.name = name;
        c.controller = controller;
        c.description = description;
        c.color = color;
        c.systemIds = systemIds;
        return c;
    }}

    private static Domain makeDomain(int id, String name, String controller, String description, String color, int[] constellationIds) {{
        Domain d = new Domain();
        d.id = id;
        d.name = name;
        d.controller = controller;
        d.description = description;
        d.color = color;
        d.constellationIds = constellationIds;
        return d;
    }}
}}
'''


def write_text(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def parse_args() -> argparse.Namespace:
    root = Path(__file__).resolve().parents[2]
    default_input = root / "tools" / "star_map_editor" / "star_map.json"
    default_out_dir = Path(__file__).resolve().parent / "output"

    p = argparse.ArgumentParser(description="Convert editor star map JSON for server/client consumption.")
    p.add_argument("--input", default=str(default_input), help="Input editor JSON path")
    p.add_argument("--out-server-json", default=str(default_out_dir / "server_star_map.json"), help="Output server JSON")
    p.add_argument("--out-client-json", default=str(default_out_dir / "client_star_map.json"), help="Output client JSON")
    p.add_argument("--out-server-py", default=str(default_out_dir / "star_map_generated.py"), help="Output Python map module")
    p.add_argument("--out-client-java", default=str(default_out_dir / "StarMapCatalogGenerated.java"), help="Output Java catalog class")
    return p.parse_args()


def main() -> int:
    args = parse_args()

    input_path = Path(args.input)
    if not input_path.exists():
        raise FileNotFoundError(f"Input file not found: {input_path}")

    src = json.loads(input_path.read_text(encoding="utf-8"))
    normalized = normalize_map(src)

    server_json = {
        "systems": normalized["systems"],
        "links": normalized["links"],
        "bodies": normalized["bodies"],
        "constellations": normalized["constellations"],
        "domains": normalized["domains"],
    }
    client_json = {
        "systems": normalized["systems"],
        "links": normalized["links"],
        "constellations": normalized["constellations"],
        "domains": normalized["domains"],
    }

    write_text(Path(args.out_server_json), json.dumps(server_json, ensure_ascii=False, indent=2))
    write_text(Path(args.out_client_json), json.dumps(client_json, ensure_ascii=False, indent=2))
    write_text(Path(args.out_server_py), make_server_py(normalized))
    write_text(Path(args.out_client_java), make_client_java(normalized))

    print("Conversion complete:")
    print(f"  systems: {len(normalized['systems'])}")
    print(f"  links:   {len(normalized['links'])}")
    print(f"  bodies:  {len(normalized['bodies'])}")
    print(f"  constellations: {len(normalized['constellations'])}")
    print(f"  domains: {len(normalized['domains'])}")
    print(f"  server json: {args.out_server_json}")
    print(f"  client json: {args.out_client_json}")
    print(f"  server py:   {args.out_server_py}")
    print(f"  client java: {args.out_client_java}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
