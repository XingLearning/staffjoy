package xyz.staffjoy.common.services;

public class SecurityConstant {

    /**
     * Public security means a user may be logged out or logged in
     * 公共安全意味着用户可能已注销或登录
     */
    public static final int SEC_PUBLIC = 0;
    /**
     * Authenticated security means a user must be logged in
     * 经过身份验证的安全性意味着用户必须登录
     */
    public static final int SEC_AUTHENTICATED = 1;
    /**
     * Admin security means a user must be both logged in and have sudo flag
     * 管理员安全意味着用户必须同时登录并拥有sudo标志
     */
    public static final int SEC_ADMIN = 2;
}
