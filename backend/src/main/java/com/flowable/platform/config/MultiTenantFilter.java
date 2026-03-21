package com.flowable.platform.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class MultiTenantFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-Tenant-Id";
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String tenantId = request.getHeader(TENANT_HEADER);

        if (tenantId != null && !tenantId.isEmpty()) {
            CURRENT_TENANT.set(tenantId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            CURRENT_TENANT.remove();
        }
    }

    public static String getCurrentTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static void setTenantContext(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static void clearTenantContext() {
        CURRENT_TENANT.remove();
    }
}
