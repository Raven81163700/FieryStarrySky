from flask import Flask, request, make_response, send_from_directory, jsonify
import io, json, os

# try to use eventlet when available for proper WebSocket support
EVENTLET_AVAILABLE = False
try:
    import eventlet
    # monkey patch stdlib for cooperative IO
    eventlet.monkey_patch()
    EVENTLET_AVAILABLE = True
except Exception:
    EVENTLET_AVAILABLE = False

from flask_socketio import SocketIO, emit

HERE = os.path.dirname(__file__)
DATA_FILE = os.path.join(HERE, 'star_map.json')

app = Flask(__name__, static_folder='static', static_url_path='', template_folder='static')
app.config['SECRET_KEY'] = 'dev'

# prefer eventlet if available for websocket support; enable logging
if EVENTLET_AVAILABLE:
    socketio = SocketIO(app, cors_allowed_origins='*', async_mode='eventlet', logger=True, engineio_logger=True)
else:
    socketio = SocketIO(app, cors_allowed_origins='*', async_mode='threading', logger=True, engineio_logger=True)


def default_state():
    return {
        'systems': [],
        'links': [],
        'bodies': [],
        'constellations': [],
        'domains': [],
    }


def normalize_state(data):
    base = default_state()
    if not isinstance(data, dict):
        return base
    for key in base:
        value = data.get(key, base[key])
        base[key] = value if isinstance(value, list) else base[key]
    return base


def load_state():
    if os.path.exists(DATA_FILE):
        with open(DATA_FILE, 'r', encoding='utf-8') as f:
            return normalize_state(json.load(f))
    return default_state()


def save_state(data):
    state = normalize_state(data)
    try:
        with open(DATA_FILE, 'w', encoding='utf-8') as f:
            json.dump(state, f, indent=2, ensure_ascii=False)
        sys_count = len(state.get('systems', []))
        con_count = len(state.get('constellations', []))
        dom_count = len(state.get('domains', []))
        print(f'[save] OK — systems={sys_count} constellations={con_count} domains={dom_count}')
    except Exception as e:
        print(f'[save] ERROR: {e}')
        raise
    return state


@app.route('/')
def index():
    return app.send_static_file('index.html')


@app.route('/export', methods=['POST'])
def export():
    data = request.get_json()
    if data is None:
        return {'error': 'invalid json'}, 400
    out = io.BytesIO(json.dumps(data, indent=2).encode('utf-8'))
    resp = make_response(out.getvalue())
    resp.headers['Content-Type'] = 'application/json'
    resp.headers['Content-Disposition'] = 'attachment; filename=star_map_export.json'
    return resp


@app.route('/saved', methods=['GET'])
def saved():
    return jsonify(load_state())


@app.route('/save', methods=['POST'])
def save_to_disk():
    data = request.get_json()
    if data is None:
        return {'error': 'invalid json'}, 400
    save_state(data)
    return {'status': 'ok'}


@socketio.on('connect')
def on_connect():
    print(f'[connect] client connected')
    emit('state', load_state())


@socketio.on('disconnect')
def on_disconnect():
    print(f'[disconnect] client disconnected')


@socketio.on('request_state')
def on_request_state():
    print('[request_state] sending current state')
    emit('state', load_state())


@socketio.on('update_state')
def on_update_state(data):
    print(f'[update_state] received, systems={len(data.get("systems", [])) if isinstance(data, dict) else "?"}')
    try:
        state = save_state(data)
        emit('state', state, broadcast=True, include_self=False)
    except Exception as e:
        print(f'[update_state] save failed: {e}')
        emit('save_error', {'message': str(e)})


@socketio.on('save')
def on_socket_save(data):
    print(f'[save] received explicit save')
    try:
        state = save_state(data)
        emit('state', state, broadcast=True, include_self=False)
        emit('saved', {'status': 'ok'})
    except Exception as e:
        print(f'[save] failed: {e}')
        emit('save_error', {'message': str(e)})


if __name__ == '__main__':
    # use socketio.run so Socket.IO server is used
    socketio.run(app, debug=True, host='0.0.0.0', port=5000)
