package com.openlib.dto.response;

public record AuthResponse(
    String        accessToken,
    String        refreshToken,
    String        tokenType,
    long          expiresIn,     // segundos
    UserResponse  user
) {
    public static AuthResponse of(String access, String refresh, long expiresIn, UserResponse user) {
        return new AuthResponse(access, refresh, "Bearer", expiresIn, user);
    }
}
