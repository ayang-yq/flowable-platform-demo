package com.flowable.platform.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@Order(2)
public class TenantIsolationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantIsolationFilter.class);

    private static final Set<String> EXCLUDED_PREFIXES = Set.of(
            "/api/auth/", "/oauth2/", "/actuator/", "/swagger-ui", "/v3/api-docs"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (!isExcludedPath(path) && path.startsWith("/api/")) {
            String tenantId = MultiTenantFilter.getCurrentTenantId();
            if (tenantId == null || tenantId.isBlank()) {
                log.warn("API request to {} without tenant context", path);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExcludedPath(String path) {
        return EXCLUDED_PREFIXES.stream().anyMatch(path::startsWith);
    }
}
