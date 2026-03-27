Star Map Editor (Prototype)

Run (quick):

1) Using an existing Python environment with pip:

   cd tools\star_map_editor
   python -m pip install -r requirements.txt
   python app.py

2) Using the included helper (creates a virtualenv):

   Windows (double-click or run in PowerShell):
     tools\star_map_editor\run_editor.bat

   macOS / Linux:
     sh tools/star_map_editor/run_editor.sh

Open in browser:
  http://127.0.0.1:5000/

Usage:
- Double-click the canvas to add a system (you'll be prompted for name and security).
- Drag systems to reposition them.
- Toggle "Link" mode and click two systems to create a link.
- Click Export to download a JSON file. The JSON format matches the server schema minimally:
  {
    "systems": [{"id": 1, "name": "Sol", "x": 120, "y":120, "security":3}, ...],
    "links": [{"from_id":1, "to_id":2, "link_type":1, "cost":5}, ...],
    "bodies": []
  }

Notes:
- This is a small prototype: it focuses on placing systems, linking them, and exporting JSON.
- You can extend the frontend to add bodies, orbit data, and finer metadata.

If you want exported maps to be directly usable by the server, copy the exported JSON into the server data area or adapt `server/star_map.py` to load the JSON.
