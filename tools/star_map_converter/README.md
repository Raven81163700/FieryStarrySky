# Star Map Converter

Convert the editor JSON (from `tools/star_map_editor/star_map.json`) into data files that fit the current server/client codebases.

The generated schema includes `systems`, `links`, `bodies`, `constellations`, `domains`.

## What It Generates

- `server_star_map.json`: normalized systems/links/bodies/constellations/domains for server-side consumption.
- `client_star_map.json`: normalized systems/links/constellations/domains for client-side consumption.
- `star_map_generated.py`: Python module with `SYSTEMS`, `LINKS`, `BODIES`, `CONSTELLATIONS`, `DOMAINS` and helper functions (compatible style with `server/star_map.py`).
- `StarMapCatalogGenerated.java`: Java class with static arrays (compatible style with `client/src/com/aurora/world/StarMapCatalog.java`).

All generated files are written into `tools/star_map_converter/output/` by default.

## Local Virtual Environment

```powershell
python -m venv tools/star_map_converter/.venv
tools/star_map_converter/.venv/Scripts/Activate.ps1
```

## Usage

From repository root:

```powershell
python tools/star_map_converter/convert.py
```

Custom paths:

```powershell
python tools/star_map_converter/convert.py \
  --input tools/star_map_editor/star_map.json \
  --out-server-json tools/star_map_converter/output/server_star_map.json \
  --out-client-json tools/star_map_converter/output/client_star_map.json \
  --out-server-py tools/star_map_converter/output/star_map_generated.py \
  --out-client-java tools/star_map_converter/output/StarMapCatalogGenerated.java
```

Write directly into server/client projects:

```powershell
python tools/star_map_converter/convert.py \
  --out-server-py server/star_map_generated.py \
  --out-client-java client/src/com/aurora/world/StarMapCatalogGenerated.java
```

## Security Mapping

Editor security is usually in `[-1, 1]` float range. The converter maps it to integer levels expected by current server/client map protocol:

- `v >= 0.67` -> `3`
- `0.0 <= v < 0.67` -> `2`
- `v < 0.0` -> `1`

If your source already uses integer security, values are preserved (clamped to `1..3`).

## Notes

- This tool does **not** modify existing server/client files unless you pass output paths pointing to those files.
- Java output escapes non-ASCII system names for source compatibility.
- If input has no `bodies`, generated body arrays will be empty.
