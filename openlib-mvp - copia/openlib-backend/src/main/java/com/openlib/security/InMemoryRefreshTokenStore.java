package com.openlib.security;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryRefreshTokenStore implements RefreshTokenStore {

    private static final InMemoryRefreshTokenStore INSTANCE = new InMemoryRefreshTokenStore();

    private final ConcurrentMap<String, RefreshTokenEntry> store = new ConcurrentHashMap<>();

    private InMemoryRefreshTokenStore() {
    }

    public static InMemoryRefreshTokenStore getInstance() {
        return INSTANCE;
    }

    @Override
    public void save(String token, String email, Instant expiresAt) {
        store.put(token, new RefreshTokenEntry(email, expiresAt));
    }

    @Override
    public Optional<RefreshTokenEntry> find(String token) {
        return Optional.ofNullable(store.get(token));
    }

    @Override
    public void remove(String token) {
        store.remove(token);
    }

    @Override
    public void cleanupExpired() {
        store.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
