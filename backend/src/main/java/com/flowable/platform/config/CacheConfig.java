package com.flowable.platform.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String USERS_CACHE = "users";
    public static final String ROLES_CACHE = "roles";
    public static final String DEPARTMENTS_CACHE = "departments";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(USERS_CACHE, ROLES_CACHE, DEPARTMENTS_CACHE);
    }

    public static String tenantKey(String tenantId, String key) {
        return "tenant:" + tenantId + ":" + key;
    }
}
