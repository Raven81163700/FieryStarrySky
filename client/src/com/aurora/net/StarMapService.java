package com.aurora.net;

import java.io.IOException;

public final class StarMapService {

    private final TcpClient tcpClient;

    public StarMapService(String host, int port) {
        this.tcpClient = new TcpClient(host, port);
    }

    public static final class StarSystem {
        public int id;
        public String name;
        public int x;
        public int y;
        public int security;
    }

    public static final class SystemLink {
        public int fromId;
        public int toId;
        public int linkType;
        public int cost;
    }

    public static final class Body {
        public int id;
        public int bodyType;
        public String name;
        public int x;
        public int y;
        public int primary;
    }

    public static final class StarMapData {
        public StarSystem[] systems;
        public SystemLink[] links;
    }

    public Result getStarMap() {
        String line;
        try {
            line = tcpClient.sendAndReadLine(GameProtocol.buildGetStarMap());
        } catch (IOException e) {
            return Result.fail("网络错误: " + e.toString());
        }
        if (line == null || line.length() == 0) {
            return Result.fail("服务器未返回数据");
        }
        String[] parts = GameProtocol.splitFields(line);
        if (parts.length < 4) {
            return Result.fail("响应字段不足");
        }
        if (!GameProtocol.CMD_OK.equals(parts[0])) {
            String reason = (parts.length >= 3) ? parts[2] : "服务器错误";
            return Result.fail(reason);
        }
        if (!GameProtocol.CMD_GET_STAR_MAP.equals(parts[1])) {
            return Result.fail("响应命令不匹配");
        }

        int idx = 2;
        int sCount = 0;
        try { sCount = Integer.parseInt(parts[idx++]); } catch (Exception e) { return Result.fail("系统数量格式错误"); }

        StarMapData data = new StarMapData();
        data.systems = new StarSystem[sCount];
        int i;
        for (i = 0; i < sCount; i++) {
            if (idx + 4 >= parts.length) return Result.fail("系统字段不足");
            StarSystem s = new StarSystem();
            try { s.id = Integer.parseInt(parts[idx++]); } catch (Exception e) { return Result.fail("系统ID格式错误"); }
            s.name = parts[idx++];
            try { s.x = Integer.parseInt(parts[idx++]); } catch (Exception e) { return Result.fail("系统X格式错误"); }
            try { s.y = Integer.parseInt(parts[idx++]); } catch (Exception e) { return Result.fail("系统Y格式错误"); }
            try { s.security = Integer.parseInt(parts[idx++]); } catch (Exception e) { return Result.fail("系统安全等级格式错误"); }
            data.systems[i] = s;
        }

        if (idx >= parts.length) return Result.fail("缺少航道数量");
        int lCount = 0;
        try { lCount = Integer.parseInt(parts[idx++]); } catch (Exception e) { return Result.fail("航道数量格式错误"); }
        data.links = new SystemLink[lCount];
        for (i = 0; i < lCount; i++) {
            if (idx + 3 >= parts.length) return Result.fail("航道字段不足");
            SystemLink l = new SystemLink();
            try { l.fromId = Integer.parseInt(parts[idx++]); } catch (Exception e) { return Result.fail("航道起点格式错误"); }
            try { l.toId = Integer.parseInt(parts[idx++]); } catch (Exception e) { return Result.fail("航道终点格式错误"); }
            try { l.linkType = Integer.parseInt(parts[idx++]); } catch (Exception e) { return Result.fail("航道类型格式错误"); }
            try { l.cost = Integer.parseInt(parts[idx++]); } catch (Exception e) { return Result.fail("航道代价格式错误"); }
            data.links[i] = l;
        }

        return Result.ok(data);
    }

    public Result getSystemBodies(int systemId) {
        String line;
        try {
            line = tcpClient.sendAndReadLine(GameProtocol.buildGetSystemBodies(systemId));
        } catch (IOException e) {
            return Result.fail("网络错误: " + e.toString());
        }
        if (line == null || line.length() == 0) {
            return Result.fail("服务器未返回数据");
        }
        String[] parts = GameProtocol.splitFields(line);
        if (parts.length < 5) {
            return Result.fail("响应字段不足");
        }
        if (!GameProtocol.CMD_OK.equals(parts[0])) {
            String reason = (parts.length >= 3) ? parts[2] : "服务器错误";
            return Result.fail(reason);
        }
        if (!GameProtocol.CMD_GET_SYSTEM_BODIES.equals(parts[1])) {
            return Result.fail("响应命令不匹配");
        }

        int idx = 2;
        int sid = 0;
        int count = 0;
        try { sid = Integer.parseInt(parts[idx++]); } catch (Exception e) { return Result.fail("系统ID格式错误"); }
        if (sid != systemId) return Result.fail("系统ID不匹配");
        try { count = Integer.parseInt(parts[idx++]); } catch (Exception e) { return Result.fail("天体数量格式错误"); }

        Body[] out = new Body[count];
        int i;
        for (i = 0; i < count; i++) {
            if (idx + 5 >= parts.length) return Result.fail("天体字段不足");
            Body b = new Body();
            try { b.id = Integer.parseInt(parts[idx++]); } catch (Exception e) { return Result.fail("天体ID格式错误"); }
            try { b.bodyType = Integer.parseInt(parts[idx++]); } catch (Exception e) { return Result.fail("天体类型格式错误"); }
            b.name = parts[idx++];
            try { b.x = Integer.parseInt(parts[idx++]); } catch (Exception e) { return Result.fail("天体X格式错误"); }
            try { b.y = Integer.parseInt(parts[idx++]); } catch (Exception e) { return Result.fail("天体Y格式错误"); }
            try { b.primary = Integer.parseInt(parts[idx++]); } catch (Exception e) { return Result.fail("天体主星格式错误"); }
            out[i] = b;
        }

        return Result.ok(out);
    }

    public static final class Result {
        private final boolean success;
        private final String message;
        private final Object data;

        private Result(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static Result ok(Object data) { return new Result(true, null, data); }
        public static Result fail(String msg) { return new Result(false, msg, null); }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
    }
}
