package com.social.utils;

public class SecurityUtil {

    // 返回固定用户ID用于测试
    public static Long getCurrentUserId() {
        System.out.println("=== SecurityUtil.getCurrentUserId 返回 1 ===");
        return 1L;
    }

    public static String getCurrentUserRole() {
        return "ROLE_USER";
    }
}