package com.aurora.auth;

import java.io.IOException;
import com.aurora.net.GameProtocol;
import com.aurora.net.TcpClient;

public final class AuthService {

    private final TcpClient tcpClient;

    public AuthService(String host, int port) {
        this.tcpClient = new TcpClient(host, port);
    }

    public AuthResult login(String username, String password) {
        return executeAuth(GameProtocol.CMD_LOGIN, GameProtocol.buildLogin(username, password));
    }

    public AuthResult register(String username, String password) {
        return executeAuth(GameProtocol.CMD_REGISTER, GameProtocol.buildRegister(username, password));
    }

    private AuthResult executeAuth(String requestCmd, String payload) {
        String line;
        try {
            line = tcpClient.sendAndReadLine(payload);
        } catch (IOException e) {
            return AuthResult.fail("网络错误: " + e.toString());
        }

        if (line == null || line.length() == 0) {
            return AuthResult.fail("服务器未返回数据");
        }

        String[] parts = GameProtocol.splitFields(line);
        if (parts.length < 1) {
            return AuthResult.fail("响应格式错误");
        }

        if (GameProtocol.CMD_OK.equals(parts[0])) {
            return parseOkResponse(requestCmd, parts);
        }
        if (GameProtocol.CMD_ERR.equals(parts[0])) {
            return parseErrResponse(requestCmd, parts);
        }
        return AuthResult.fail("未知响应: " + line);
    }

    private AuthResult parseOkResponse(String requestCmd, String[] parts) {
        if (parts.length < 2) {
            return AuthResult.fail("响应格式错误");
        }
        if (!requestCmd.equals(parts[1])) {
            return AuthResult.fail("响应命令不匹配: " + parts[1]);
        }

        if (GameProtocol.CMD_LOGIN.equals(requestCmd)) {
            if (parts.length < 4) {
                return AuthResult.fail("登录响应字段不足");
            }
            try {
                int accountId = Integer.parseInt(parts[2]);
                String username = parts[3];
                return AuthResult.success(accountId, username, "登录成功");
            } catch (NumberFormatException e) {
                return AuthResult.fail("账号ID格式错误");
            }
        }

        if (GameProtocol.CMD_REGISTER.equals(requestCmd)) {
            if (parts.length < 3) {
                return AuthResult.fail("注册响应字段不足");
            }
            try {
                int accountId = Integer.parseInt(parts[2]);
                return AuthResult.success(accountId, null, "注册成功, 账号ID=" + accountId);
            } catch (NumberFormatException e) {
                return AuthResult.fail("账号ID格式错误");
            }
        }

        return AuthResult.fail("不支持的命令: " + requestCmd);
    }

    private AuthResult parseErrResponse(String requestCmd, String[] parts) {
        if (parts.length < 3) {
            return AuthResult.fail("服务器返回错误, 但缺少原因");
        }
        String serverCmd = parts[1];
        String reason = parts[2];
        if (!requestCmd.equals(serverCmd)) {
            return AuthResult.fail("服务器错误命令不匹配: " + serverCmd + ", 原因: " + reason);
        }
        return AuthResult.fail(reason);
    }
}
