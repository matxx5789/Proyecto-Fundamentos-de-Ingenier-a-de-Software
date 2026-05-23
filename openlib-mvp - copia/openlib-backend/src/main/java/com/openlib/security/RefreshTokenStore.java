package com.openlib.security;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenStore {

    void save(String token, String email, Instant expiresAt);

    Optional<RefreshTokenEntry> find(String token);

    void remove(String token);

    void cleanupExpired();

    record RefreshTokenEntry(String email, Instant expiresAt) {
        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
