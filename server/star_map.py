# -*- coding: utf-8 -*-
"""
Aurora Online Game Server - Static Star Map Data

This module keeps the fixed core star map in code.
Dynamic/special bodies can be layered later by event systems.
"""

# Fixed star systems (id, name, world x/y, security level)
SYSTEMS = [
    {'id': 1,  'name': 'Sol',       'x': 120,  'y': 120, 'security': 3},
    {'id': 2,  'name': 'Alpha',     'x': 300,  'y': 90,  'security': 2},
    {'id': 3,  'name': 'Cygnus',    'x': 520,  'y': 130, 'security': 1},
    {'id': 4,  'name': 'Orion',     'x': 260,  'y': 280, 'security': 2},
    {'id': 5,  'name': 'Perseus',   'x': 480,  'y': 300, 'security': 1},
    {'id': 6,  'name': 'Vega',      'x': 700,  'y': 110, 'security': 2},
    {'id': 7,  'name': 'Sirius',    'x': 860,  'y': 170, 'security': 2},
    {'id': 8,  'name': 'Deneb',     'x': 1040, 'y': 120, 'security': 1},
    {'id': 9,  'name': 'Rigel',     'x': 760,  'y': 320, 'security': 1},
    {'id': 10, 'name': 'Antares',   'x': 980,  'y': 350, 'security': 1},
    {'id': 11, 'name': 'Polaris',   'x': 1220, 'y': 150, 'security': 3},
    {'id': 12, 'name': 'Arcturus',  'x': 1320, 'y': 300, 'security': 2},
    {'id': 13, 'name': 'Altair',    'x': 560,  'y': 470, 'security': 2},
    {'id': 14, 'name': 'Bellatrix', 'x': 900,  'y': 500, 'security': 1},
]

# Fixed links between systems (bidirectional for now)
LINKS = [
    {'from_id': 1, 'to_id': 2,  'link_type': 1, 'cost': 5},
    {'from_id': 2, 'to_id': 3,  'link_type': 1, 'cost': 6},
    {'from_id': 1, 'to_id': 4,  'link_type': 1, 'cost': 4},
    {'from_id': 4, 'to_id': 5,  'link_type': 1, 'cost': 5},
    {'from_id': 2, 'to_id': 4,  'link_type': 1, 'cost': 3},
    {'from_id': 3, 'to_id': 6,  'link_type': 1, 'cost': 4},
    {'from_id': 6, 'to_id': 7,  'link_type': 1, 'cost': 3},
    {'from_id': 7, 'to_id': 8,  'link_type': 1, 'cost': 3},
    {'from_id': 6, 'to_id': 9,  'link_type': 1, 'cost': 4},
    {'from_id': 9, 'to_id': 10, 'link_type': 1, 'cost': 4},
    {'from_id': 8, 'to_id': 11, 'link_type': 1, 'cost': 5},
    {'from_id': 11,'to_id': 12, 'link_type': 1, 'cost': 4},
    {'from_id': 5, 'to_id': 13, 'link_type': 1, 'cost': 4},
    {'from_id': 9, 'to_id': 13, 'link_type': 1, 'cost': 5},
    {'from_id': 10,'to_id': 14, 'link_type': 1, 'cost': 3},
    {'from_id': 13,'to_id': 14, 'link_type': 1, 'cost': 5},
]

# Fixed major bodies inside each system
# body_type: 1=star, 2=planet, 3=station, 4=belt
BODIES = [
    {'id': 1001, 'system_id': 1,  'body_type': 1, 'name': 'Sol-A',         'x': 58, 'y': 44, 'primary': 0},
    {'id': 1002, 'system_id': 1,  'body_type': 2, 'name': 'Sol-I',         'x': 28, 'y': 58, 'primary': 1001},
    {'id': 1003, 'system_id': 1,  'body_type': 4, 'name': 'Sol-Belt',      'x': 64, 'y': 70, 'primary': 1001},
    {'id': 1004, 'system_id': 1,  'body_type': 3, 'name': 'Sol-Station',   'x': 90, 'y': 54, 'primary': 1001},

    {'id': 2001, 'system_id': 2,  'body_type': 1, 'name': 'Alpha-A',       'x': 60, 'y': 40, 'primary': 0},
    {'id': 2002, 'system_id': 2,  'body_type': 2, 'name': 'Alpha-I',       'x': 34, 'y': 60, 'primary': 2001},
    {'id': 2003, 'system_id': 2,  'body_type': 4, 'name': 'Alpha-Belt',    'x': 66, 'y': 72, 'primary': 2001},
    {'id': 2004, 'system_id': 2,  'body_type': 3, 'name': 'Alpha-Station', 'x': 92, 'y': 50, 'primary': 2001},

    {'id': 3001, 'system_id': 3,  'body_type': 1, 'name': 'Cygnus-A',       'x': 56, 'y': 44, 'primary': 0},
    {'id': 3002, 'system_id': 3,  'body_type': 2, 'name': 'Cygnus-I',       'x': 24, 'y': 52, 'primary': 3001},
    {'id': 3003, 'system_id': 3,  'body_type': 4, 'name': 'Cygnus-Belt',    'x': 64, 'y': 68, 'primary': 3001},
    {'id': 3004, 'system_id': 3,  'body_type': 3, 'name': 'Cygnus-Station', 'x': 88, 'y': 56, 'primary': 3001},

    {'id': 4001, 'system_id': 4,  'body_type': 1, 'name': 'Orion-A',       'x': 58, 'y': 40, 'primary': 0},
    {'id': 4002, 'system_id': 4,  'body_type': 2, 'name': 'Orion-I',       'x': 30, 'y': 62, 'primary': 4001},
    {'id': 4003, 'system_id': 4,  'body_type': 4, 'name': 'Orion-Belt',    'x': 66, 'y': 70, 'primary': 4001},
    {'id': 4004, 'system_id': 4,  'body_type': 3, 'name': 'Orion-Station', 'x': 92, 'y': 50, 'primary': 4001},

    {'id': 5001, 'system_id': 5,  'body_type': 1, 'name': 'Perseus-A',       'x': 62, 'y': 42, 'primary': 0},
    {'id': 5002, 'system_id': 5,  'body_type': 2, 'name': 'Perseus-I',       'x': 30, 'y': 58, 'primary': 5001},
    {'id': 5003, 'system_id': 5,  'body_type': 4, 'name': 'Perseus-Belt',    'x': 66, 'y': 72, 'primary': 5001},
    {'id': 5004, 'system_id': 5,  'body_type': 3, 'name': 'Perseus-Station', 'x': 94, 'y': 54, 'primary': 5001},

    {'id': 6001, 'system_id': 6,  'body_type': 1, 'name': 'Vega-A',       'x': 58, 'y': 40, 'primary': 0},
    {'id': 6002, 'system_id': 6,  'body_type': 2, 'name': 'Vega-I',       'x': 32, 'y': 56, 'primary': 6001},
    {'id': 6003, 'system_id': 6,  'body_type': 4, 'name': 'Vega-Belt',    'x': 66, 'y': 72, 'primary': 6001},
    {'id': 6004, 'system_id': 6,  'body_type': 3, 'name': 'Vega-Station', 'x': 90, 'y': 52, 'primary': 6001},

    {'id': 7001, 'system_id': 7,  'body_type': 1, 'name': 'Sirius-A',       'x': 60, 'y': 44, 'primary': 0},
    {'id': 7002, 'system_id': 7,  'body_type': 2, 'name': 'Sirius-I',       'x': 28, 'y': 58, 'primary': 7001},
    {'id': 7003, 'system_id': 7,  'body_type': 4, 'name': 'Sirius-Belt',    'x': 66, 'y': 70, 'primary': 7001},
    {'id': 7004, 'system_id': 7,  'body_type': 3, 'name': 'Sirius-Station', 'x': 92, 'y': 52, 'primary': 7001},

    {'id': 8001, 'system_id': 8,  'body_type': 1, 'name': 'Deneb-A',       'x': 58, 'y': 40, 'primary': 0},
    {'id': 8002, 'system_id': 8,  'body_type': 2, 'name': 'Deneb-I',       'x': 30, 'y': 56, 'primary': 8001},
    {'id': 8003, 'system_id': 8,  'body_type': 4, 'name': 'Deneb-Belt',    'x': 64, 'y': 70, 'primary': 8001},
    {'id': 8004, 'system_id': 8,  'body_type': 3, 'name': 'Deneb-Station', 'x': 90, 'y': 52, 'primary': 8001},

    {'id': 9001, 'system_id': 9,  'body_type': 1, 'name': 'Rigel-A',       'x': 58, 'y': 42, 'primary': 0},
    {'id': 9002, 'system_id': 9,  'body_type': 2, 'name': 'Rigel-I',       'x': 30, 'y': 58, 'primary': 9001},
    {'id': 9003, 'system_id': 9,  'body_type': 4, 'name': 'Rigel-Belt',    'x': 64, 'y': 70, 'primary': 9001},
    {'id': 9004, 'system_id': 9,  'body_type': 3, 'name': 'Rigel-Station', 'x': 92, 'y': 54, 'primary': 9001},

    {'id': 10001, 'system_id': 10, 'body_type': 1, 'name': 'Antares-A',       'x': 60, 'y': 42, 'primary': 0},
    {'id': 10002, 'system_id': 10, 'body_type': 2, 'name': 'Antares-I',       'x': 28, 'y': 56, 'primary': 10001},
    {'id': 10003, 'system_id': 10, 'body_type': 4, 'name': 'Antares-Belt',    'x': 66, 'y': 70, 'primary': 10001},
    {'id': 10004, 'system_id': 10, 'body_type': 3, 'name': 'Antares-Station', 'x': 92, 'y': 54, 'primary': 10001},

    {'id': 11001, 'system_id': 11, 'body_type': 1, 'name': 'Polaris-A',       'x': 58, 'y': 40, 'primary': 0},
    {'id': 11002, 'system_id': 11, 'body_type': 2, 'name': 'Polaris-I',       'x': 30, 'y': 56, 'primary': 11001},
    {'id': 11003, 'system_id': 11, 'body_type': 4, 'name': 'Polaris-Belt',    'x': 64, 'y': 68, 'primary': 11001},
    {'id': 11004, 'system_id': 11, 'body_type': 3, 'name': 'Polaris-Station', 'x': 90, 'y': 52, 'primary': 11001},

    {'id': 12001, 'system_id': 12, 'body_type': 1, 'name': 'Arcturus-A',       'x': 60, 'y': 44, 'primary': 0},
    {'id': 12002, 'system_id': 12, 'body_type': 2, 'name': 'Arcturus-I',       'x': 30, 'y': 58, 'primary': 12001},
    {'id': 12003, 'system_id': 12, 'body_type': 4, 'name': 'Arcturus-Belt',    'x': 66, 'y': 72, 'primary': 12001},
    {'id': 12004, 'system_id': 12, 'body_type': 3, 'name': 'Arcturus-Station', 'x': 92, 'y': 54, 'primary': 12001},

    {'id': 13001, 'system_id': 13, 'body_type': 1, 'name': 'Altair-A',       'x': 58, 'y': 42, 'primary': 0},
    {'id': 13002, 'system_id': 13, 'body_type': 2, 'name': 'Altair-I',       'x': 30, 'y': 58, 'primary': 13001},
    {'id': 13003, 'system_id': 13, 'body_type': 4, 'name': 'Altair-Belt',    'x': 66, 'y': 70, 'primary': 13001},
    {'id': 13004, 'system_id': 13, 'body_type': 3, 'name': 'Altair-Station', 'x': 92, 'y': 54, 'primary': 13001},

    {'id': 14001, 'system_id': 14, 'body_type': 1, 'name': 'Bellatrix-A',       'x': 60, 'y': 44, 'primary': 0},
    {'id': 14002, 'system_id': 14, 'body_type': 2, 'name': 'Bellatrix-I',       'x': 30, 'y': 58, 'primary': 14001},
    {'id': 14003, 'system_id': 14, 'body_type': 4, 'name': 'Bellatrix-Belt',    'x': 66, 'y': 72, 'primary': 14001},
    {'id': 14004, 'system_id': 14, 'body_type': 3, 'name': 'Bellatrix-Station', 'x': 92, 'y': 54, 'primary': 14001},
]


def all_systems():
    return SYSTEMS


def all_links():
    return LINKS


def bodies_by_system(system_id: int):
    return [b for b in BODIES if b['system_id'] == system_id]


def system_exists(system_id: int) -> bool:
    for s in SYSTEMS:
        if s['id'] == system_id:
            return True
    return False
