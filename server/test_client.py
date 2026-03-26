# -*- coding: utf-8 -*-
"""
Aurora Online Game Server - 集成测试脚本
直接通过 TCP Socket 模拟 JavaME 客户端，验证完整协议交互。
用法: python test_client.py   (需要先启动 main.py)
"""

import socket
import time

HOST = "127.0.0.1"
PORT = 9000
ENCODING = "utf-8"


class TestClient:
    def __init__(self, name="client"):
        self.name = name
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect((HOST, PORT))
        self.sock.settimeout(3)
        self._buf = ""

    def send(self, msg: str):
        raw = (msg + "\n").encode(ENCODING)
        self.sock.sendall(raw)
        print("  [{}>] {}".format(self.name, msg))

    def recv(self) -> str:
        while "\n" not in self._buf:
            chunk = self.sock.recv(1024).decode(ENCODING, errors="replace")
            if not chunk:
                break
            self._buf += chunk
        line, self._buf = self._buf.split("\n", 1)
        print("  [<{}] {}".format(self.name, line))
        return line

    def close(self):
        self.sock.close()


# ─────────────────────────────────────────────────
#  断言工具
# ─────────────────────────────────────────────────

_passed = 0
_failed = 0


def expect(label: str, actual: str, starts_with: str):
    global _passed, _failed
    if actual.startswith(starts_with):
        print("  ✓ {}".format(label))
        _passed += 1
    else:
        print("  ✗ {} — 期望以 '{}' 开头，实际: '{}'".format(label, starts_with, actual))
        _failed += 1


# ─────────────────────────────────────────────────
#  测试用例
# ─────────────────────────────────────────────────

def test_ping():
    print("\n[TEST] PING")
    c = TestClient("ping")
    c.send("PING")
    expect("PING_ACK", c.recv(), "PING_ACK")
    c.close()


def test_register_and_login():
    print("\n[TEST] 注册 + 登录")
    c = TestClient("auth")

    # 正常注册
    c.send("REGISTER|tc_user1|mypass1")
    expect("注册成功", c.recv(), "OK|REGISTER")

    # 重复注册
    c.send("REGISTER|tc_user1|mypass1")
    expect("重复注册被拒", c.recv(), "ERR|REGISTER")

    # 用户名太短
    c.send("REGISTER|ab|mypass1")
    expect("用户名太短被拒", c.recv(), "ERR|REGISTER")

    # 密码太短
    c.send("REGISTER|tc_user2|123")
    expect("密码太短被拒", c.recv(), "ERR|REGISTER")

    # 正常登录
    c.send("LOGIN|tc_user1|mypass1")
    resp = c.recv()
    expect("登录成功", resp, "OK|LOGIN")

    # 错误密码
    c.send("LOGIN|tc_user1|wrongpwd")
    expect("错误密码被拒", c.recv(), "ERR|LOGIN")

    # 不存在的账号
    c.send("LOGIN|nobody|mypass1")
    expect("账号不存在被拒", c.recv(), "ERR|LOGIN")

    c.close()


def test_auth_required():
    print("\n[TEST] 未登录访问受保护命令")
    c = TestClient("noauth")
    c.send("CHAT|hello")         # CHAT 是受保护命令（未注册处理器，走未知命令路径）
    expect("未知命令被拒", c.recv(), "ERR|")
    c.close()


def test_duplicate_login():
    print("\n[TEST] 同账号重复登录 (单点踢出)")
    # 先注册
    setup = TestClient("setup")
    setup.send("REGISTER|tc_kick|kickpass")
    setup.recv()
    setup.close()
    time.sleep(0.1)

    c1 = TestClient("client1")
    c1.send("LOGIN|tc_kick|kickpass")
    expect("client1 登录成功", c1.recv(), "OK|LOGIN")

    c2 = TestClient("client2")
    c2.send("LOGIN|tc_kick|kickpass")
    # c2 登录成功，c1 应该收到被踢通知
    resp_c2 = c2.recv()
    expect("client2 登录成功", resp_c2, "OK|LOGIN")

    try:
        kicked = c1.recv()
        expect("client1 被踢出", kicked, "ERR|LOGIN")
    except socket.timeout:
        print("  ✗ client1 未收到踢出消息 (超时)")
        global _failed
        _failed += 1

    c1.close()
    c2.close()


def test_field_separator_injection():
    print("\n[TEST] 字段分隔符注入防御")
    c = TestClient("inject")
    # reason 中含 | 应被替换
    c.send("LOGIN|hacker|bad|extra|fields")
    resp = c.recv()
    # 能收到响应（不崩溃）即通过
    expect("注入不崩溃", resp, "ERR|LOGIN")
    c.close()


# ─────────────────────────────────────────────────
#  主流程
# ─────────────────────────────────────────────────

if __name__ == "__main__":
    print("=" * 50)
    print("Aurora Server 集成测试  {}:{}".format(HOST, PORT))
    print("=" * 50)

    try:
        test_ping()
        test_register_and_login()
        test_auth_required()
        test_duplicate_login()
        test_field_separator_injection()
    except ConnectionRefusedError:
        print("\n❌ 无法连接到服务器，请先运行: python main.py")
        raise

    print("\n" + "=" * 50)
    print("结果: {} 通过  {} 失败".format(_passed, _failed))
    print("=" * 50)
    if _failed:
        raise SystemExit(1)
