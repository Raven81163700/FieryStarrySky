#!/bin/sh
cd "$(dirname "$0")"
python3 -m venv .venv || python -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
python app.py
