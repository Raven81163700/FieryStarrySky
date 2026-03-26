package com.aurora.net;

public final class GameProtocol {

    public static final String FIELD_SEPARATOR = "|";
    public static final char MSG_SEPARATOR = '\n';

    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_REGISTER = "REGISTER";
    public static final String CMD_PING = "PING";

    public static final String CMD_OK = "OK";
    public static final String CMD_ERR = "ERR";
    public static final String CMD_PING_ACK = "PING_ACK";

    private GameProtocol() {
    }

    public static String buildLogin(String username, String password) {
        return CMD_LOGIN + FIELD_SEPARATOR + username + FIELD_SEPARATOR + password + MSG_SEPARATOR;
    }

    public static String buildRegister(String username, String password) {
        return CMD_REGISTER + FIELD_SEPARATOR + username + FIELD_SEPARATOR + password + MSG_SEPARATOR;
    }

    public static String buildPing() {
        return CMD_PING + MSG_SEPARATOR;
    }

    public static String[] splitFields(String line) {
        int count = 1;
        int p = line.indexOf(FIELD_SEPARATOR);
        while (p >= 0) {
            count++;
            p = line.indexOf(FIELD_SEPARATOR, p + 1);
        }

        String[] result = new String[count];
        int start = 0;
        int idx;
        int i = 0;
        while ((idx = line.indexOf(FIELD_SEPARATOR, start)) >= 0) {
            result[i++] = line.substring(start, idx);
            start = idx + 1;
        }
        result[i] = line.substring(start);
        return result;
    }
}
