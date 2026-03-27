from flask import Flask, request, make_response, send_from_directory, jsonify
from flask_socketio import SocketIO, emit
import io, json, os

HERE = os.path.dirname(__file__)
DATA_FILE = os.path.join(HERE, 'star_map.json')

app = Flask(__name__, static_folder='static', template_folder='static')
app.config['SECRET_KEY'] = 'dev'
socketio = SocketIO(app, cors_allowed_origins='*', async_mode='threading')


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
    with open(DATA_FILE, 'w', encoding='utf-8') as f:
        json.dump(state, f, indent=2, ensure_ascii=False)
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
    emit('state', load_state())


@socketio.on('request_state')
def on_request_state():
    emit('state', load_state())


@socketio.on('update_state')
def on_update_state(data):
    state = save_state(data)
    emit('state', state, broadcast=True, include_self=False)


@socketio.on('save')
def on_socket_save(data):
    state = save_state(data)
    emit('state', state, broadcast=True, include_self=False)
    emit('saved', {'status': 'ok'}, broadcast=True)


if __name__ == '__main__':
    # use socketio.run so Socket.IO server is used
    socketio.run(app, debug=True, host='0.0.0.0', port=5000)
