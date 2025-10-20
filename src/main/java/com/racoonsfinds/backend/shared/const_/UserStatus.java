package com.racoonsfinds.backend.shared.const_;

public final class UserStatus {
    public static final int NOT_FOUND = 0;
    public static final int NOT_AUTH = 1;
    public static final int AUTH_SUCCESS = 2;
    public static final int BLOCKED_TEMP = 3;
    public static final int BLOCKED_PERM = 4;
    public static final int NOT_VERIFIED = 5;
    
    private UserStatus() {} 
}