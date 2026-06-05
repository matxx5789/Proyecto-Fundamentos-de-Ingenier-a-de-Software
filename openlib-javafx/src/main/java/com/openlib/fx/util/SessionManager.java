package com.openlib.fx.util;

/**
 * Gestiona la sesión del usuario actual (token JWT + info básica).
 */
public class SessionManager {

    private static String accessToken;
    private static String userEmail;
    private static String userRole;
    private static String userFullName;
    private static Long userId;

    public static void setSession(String token, String email, String role, String fullName, Long id) {
        accessToken = token;
        userEmail = email;
        userRole = role;
        userFullName = fullName;
        userId = id;
    }

    public static void clear() {
        accessToken = null;
        userEmail = null;
        userRole = null;
        userFullName = null;
        userId = null;
    }

    public static boolean isLoggedIn() {
        return accessToken != null && !accessToken.isBlank();
    }

    public static String getAccessToken() { return accessToken; }
    public static String getUserEmail() { return userEmail; }
    public static String getUserRole() { return userRole; }
    public static String getUserFullName() { return userFullName; }
    public static Long getUserId() { return userId; }

    public static boolean isAdmin() { return "ADMIN".equals(userRole); }
    public static boolean isSeller() { return "SELLER".equals(userRole); }
    public static boolean isBuyer() { return "BUYER".equals(userRole); }

    /** Retorna el header de Authorization listo para usar. */
    public static String getBearerHeader() {
        return "Bearer " + accessToken;
    }
}
