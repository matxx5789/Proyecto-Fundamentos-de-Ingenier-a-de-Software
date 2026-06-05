package com.openlib;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
public class OpenLibApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenLibApplication.class, args);
    }
}
