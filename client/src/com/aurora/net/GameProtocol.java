package com.aurora.net;

public final class GameProtocol {

    public static final String FIELD_SEPARATOR = "|";
    public static final char MSG_SEPARATOR = '\n';

    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_REGISTER = "REGISTER";
    public static final String CMD_PING = "PING";
    public static final String CMD_LIST_CHARS = "LIST_CHARS";
    public static final String CMD_CREATE_CHAR = "CREATE_CHAR";
    public static final String CMD_DELETE_CHAR = "DELETE_CHAR";
    public static final String CMD_GET_STAR_MAP = "GET_STAR_MAP";
    public static final String CMD_GET_SYSTEM_BODIES = "GET_SYSTEM_BODIES";

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

    public static String buildListChars(int accountId) {
        return CMD_LIST_CHARS + FIELD_SEPARATOR + accountId + MSG_SEPARATOR;
    }

    public static String buildCreateChar(int accountId, String name, int gender, int backgroundId, int characterId) {
        return CMD_CREATE_CHAR + FIELD_SEPARATOR + accountId + FIELD_SEPARATOR + name + FIELD_SEPARATOR + gender + FIELD_SEPARATOR + backgroundId + FIELD_SEPARATOR + characterId + MSG_SEPARATOR;
    }

    public static String buildDeleteChar(int accountId, int charId) {
        return CMD_DELETE_CHAR + FIELD_SEPARATOR + accountId + FIELD_SEPARATOR + charId + MSG_SEPARATOR;
    }

    public static String buildGetStarMap() {
        return CMD_GET_STAR_MAP + MSG_SEPARATOR;
    }

    public static String buildGetSystemBodies(int systemId) {
        return CMD_GET_SYSTEM_BODIES + FIELD_SEPARATOR + systemId + MSG_SEPARATOR;
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
