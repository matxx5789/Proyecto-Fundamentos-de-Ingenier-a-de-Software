package com.openlib.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfig {

    @Bean
    public RefreshTokenStore refreshTokenStore() {
        return InMemoryRefreshTokenStore.getInstance();
    }
}
