package com.aurora.auth;

public final class AuthResult {

    private final boolean success;
    private final String message;
    private final int accountId;
    private final String username;

    private AuthResult(boolean success, String message, int accountId, String username) {
        this.success = success;
        this.message = message;
        this.accountId = accountId;
        this.username = username;
    }

    public static AuthResult success(int accountId, String username, String message) {
        return new AuthResult(true, message, accountId, username);
    }

    public static AuthResult fail(String message) {
        return new AuthResult(false, message, -1, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getUsername() {
        return username;
    }
}
