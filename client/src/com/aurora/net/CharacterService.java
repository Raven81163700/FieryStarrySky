package com.aurora.net;

import java.io.IOException;
import java.util.Vector;

public final class CharacterService {

    private final TcpClient tcpClient;

    public CharacterService(String host, int port) {
        this.tcpClient = new TcpClient(host, port);
    }

    public static final class CharInfo {
        public int id;
        public String name;
        public int gender;
        public int backgroundId;
        public int characterId;
        public String backgroundPath;
        public String characterPath;
        public String previewBase64;
    }

    public Result listCharacters(int accountId) {
        String line;
        try {
            line = tcpClient.sendAndReadLine(GameProtocol.buildListChars(accountId));
        } catch (IOException e) {
            return Result.fail("网络错误: " + e.toString());
        }
        if (line == null || line.length() == 0) {
            return Result.fail("服务器未返回数据");
        }
        String[] parts = GameProtocol.splitFields(line);
        if (parts.length < 1) {
            return Result.fail("响应格式错误");
        }
        if (GameProtocol.CMD_OK.equals(parts[0])) {
            if (parts.length < 3) {
                return Result.fail("响应字段不足");
            }
            // parts[1] == LIST_CHARS
            int n = 0;
            try { n = Integer.parseInt(parts[2]); } catch (Exception e) { n = 0; }
            Vector out = new Vector();
            int idx = 3;
            for (int i = 0; i < n; i++) {
                if (idx + 5 >= parts.length) break;
                CharInfo ci = new CharInfo();
                try { ci.id = Integer.parseInt(parts[idx++]); } catch (Exception e) { ci.id = 0; }
                ci.name = parts[idx++];
                try { ci.gender = Integer.parseInt(parts[idx++]); } catch (Exception e) { ci.gender = 0; }
                ci.backgroundPath = parts[idx++];
                ci.characterPath = parts[idx++];
                ci.backgroundId = -1;
                ci.characterId = -1;
                try { ci.backgroundId = Integer.parseInt(ci.backgroundPath); } catch (Exception e) { ci.backgroundId = -1; }
                try { ci.characterId = Integer.parseInt(ci.characterPath); } catch (Exception e) { ci.characterId = -1; }
                ci.previewBase64 = parts[idx++];
                out.addElement(ci);
            }
            return Result.ok(out);
        }
        if (GameProtocol.CMD_ERR.equals(parts[0])) {
            String reason = parts.length >= 3 ? parts[2] : "服务器错误";
            return Result.fail(reason);
        }
        return Result.fail("未知响应: " + line);
    }

    public Result createCharacter(int accountId, String name, int gender, int backgroundId, int characterId) {
        String line;
        try {
            line = tcpClient.sendAndReadLine(GameProtocol.buildCreateChar(accountId, name, gender, backgroundId, characterId));
        } catch (IOException e) {
            return Result.fail("网络错误: " + e.toString());
        }
        if (line == null || line.length() == 0) {
            return Result.fail("服务器未返回数据");
        }
        String[] parts = GameProtocol.splitFields(line);
        if (parts.length < 1) {
            return Result.fail("响应格式错误");
        }
        if (GameProtocol.CMD_OK.equals(parts[0])) {
            if (parts.length < 3) return Result.fail("响应字段不足");
            try {
                int charId = Integer.parseInt(parts[2]);
                return Result.ok(new Integer(charId));
            } catch (Exception e) {
                return Result.fail("角色ID格式错误");
            }
        }
        if (GameProtocol.CMD_ERR.equals(parts[0])) {
            String reason = parts.length >= 3 ? parts[2] : "服务器错误";
            return Result.fail(reason);
        }
        return Result.fail("未知响应: " + line);
    }

    public Result deleteCharacter(int accountId, int charId) {
        String line;
        try {
            line = tcpClient.sendAndReadLine(GameProtocol.buildDeleteChar(accountId, charId));
        } catch (IOException e) {
            return Result.fail("网络错误: " + e.toString());
        }
        if (line == null || line.length() == 0) {
            return Result.fail("服务器未返回数据");
        }
        String[] parts = GameProtocol.splitFields(line);
        if (parts.length < 1) {
            return Result.fail("响应格式错误");
        }
        if (GameProtocol.CMD_OK.equals(parts[0])) {
            if (parts.length < 3) return Result.fail("响应字段不足");
            try {
                int id = Integer.parseInt(parts[2]);
                return Result.ok(new Integer(id));
            } catch (Exception e) {
                return Result.fail("返回 ID 格式错误");
            }
        }
        if (GameProtocol.CMD_ERR.equals(parts[0])) {
            String reason = parts.length >= 3 ? parts[2] : "服务器错误";
            return Result.fail(reason);
        }
        return Result.fail("未知响应: " + line);
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
