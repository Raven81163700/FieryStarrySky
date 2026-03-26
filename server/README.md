# Aurora Online Game - 服务端

Python TCP 游戏服务器，对接 JavaME 客户端。

## 环境要求

- Python 3.8+（标准库，无需额外安装依赖）

## 启动服务器

```powershell
cd server
python main.py
```

默认监听 `0.0.0.0:9000`，可在 `config.py` 中修改。

## 运行集成测试

```powershell
# 先启动服务器，再另开终端运行
cd server
python test_client.py
```

---

## 文件结构

```
server/
  main.py        ← TCP 服务器入口（每连接一线程）
  config.py      ← 配置常量（端口、路径、编码）
  db.py          ← SQLite 数据库层（账号 CRUD）
  protocol.py    ← 协议解析/封包
  session.py     ← 在线会话管理（单点登录踢出）
  handlers.py    ← 消息处理器（分发表）
  test_client.py ← TCP 集成测试脚本
  data/          ← SQLite 数据库文件（aurora.db）
  logs/          ← 运行日志（server.log）
```

---

## 协议格式

**文本协议**，字段用 `|` 分隔，消息以 `\n` 结尾，UTF-8 编码。

### 客户端 → 服务端

| 消息 | 格式 | 说明 |
|------|------|------|
| 注册 | `REGISTER\|username\|password\n` | 用户名 3-16 位字母数字下划线；密码 6-32 位 |
| 登录 | `LOGIN\|username\|password\n` | |
| 心跳 | `PING\n` | 保活 |

### 服务端 → 客户端

| 消息 | 格式 | 说明 |
|------|------|------|
| 成功 | `OK\|CMD\|...附加字段\n` | 例: `OK\|LOGIN\|1\|alice` |
| 失败 | `ERR\|CMD\|原因\n` | 例: `ERR\|LOGIN\|密码错误` |
| 心跳回复 | `PING_ACK\n` | |

---

## 数据库 Schema

```sql
-- 账号表
accounts(id, username, pass_hash, created_at, last_login)

-- 角色表（预留）
characters(id, account_id, name, level, exp, map_id, x, y, created_at)
```

密码存储：SHA-256 + 固定盐（`aurora_salt_v1:` 前缀），十六进制字符串。

---

## 待实现

- [ ] 角色创建 / 选择
- [ ] 地图/场景同步
- [ ] 玩家移动广播
- [ ] 聊天系统
- [ ] 战斗逻辑
