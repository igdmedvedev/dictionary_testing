package com.testing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {
    public record GameProperties(String path, Integer from, Integer count) {

    }

    @Bean
    public GameProperties wordsProperties(Environment env) {
        String path = env.getProperty("words.path", String.class);
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("words.path is not set");
        }

        Integer from = env.getProperty("word.from", Integer.class, 1);
        if (from < 1) {
            throw new IllegalArgumentException("words.from must be >= 1, got: " + from);
        }

        Integer count = env.getProperty("words.count", Integer.class); // null = все слова
        if (count != null && count < 1) {
            throw new IllegalArgumentException("words.count must be positive, got: " + count);
        }

        return new GameProperties(path, from, count);
    }
}