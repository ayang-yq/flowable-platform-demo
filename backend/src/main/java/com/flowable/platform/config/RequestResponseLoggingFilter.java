package com.flowable.platform.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String requestId = java.util.UUID.randomUUID().toString().substring(0, 8);

        // Wrap request FIRST to cache body for logging
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);
        CachedBodyHttpServletResponse wrappedResponse = new CachedBodyHttpServletResponse(response);

        // Log request (after wrapping so we can read the body)
        logRequest(wrappedRequest, requestId);

        try {
            // Continue filter chain
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            // Log response
            long duration = System.currentTimeMillis() - startTime;
            logResponse(wrappedResponse, requestId, duration);
        }
    }

    private void logRequest(CachedBodyHttpServletRequest request, String requestId) {
        StringBuilder log = new StringBuilder();
        log.append("\n");
        log.append("=== ").append(requestId).append(" INCOMING REQUEST ===\n");
        log.append("Method: ").append(request.getMethod()).append("\n");
        log.append("URI: ").append(request.getRequestURI()).append("\n");
        log.append("Query String: ").append(request.getQueryString() != null ? request.getQueryString() : "none").append("\n");

        // Log headers
        log.append("Headers:\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            // Mask Authorization header for security
            if ("Authorization".equalsIgnoreCase(headerName)) {
                headerValue = maskToken(headerValue);
            }
            log.append("  ").append(headerName).append(": ").append(headerValue).append("\n");
        }

        // Log request body (for POST/PUT requests)
        try {
            if ("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod())) {
                String requestBody = new String(request.getCachedBody(), StandardCharsets.UTF_8);
                if (!requestBody.isEmpty()) {
                    String truncatedBody = requestBody;
                    if (requestBody.length() > 500) {
                        truncatedBody = requestBody.substring(0, 500) + "... (truncated)";
                    }
                    log.append("Request Body: ").append(truncatedBody).append("\n");
                } else {
                    log.append("Request Body: (empty)\n");
                }
            }
        } catch (Exception e) {
            log.append("Request Body: (error reading body: ").append(e.getMessage()).append(")\n");
        }

        // Log authentication info
        log.append("Authentication: ");
        if (request.getUserPrincipal() != null) {
            log.append(request.getUserPrincipal().getName()).append("\n");
        } else {
            log.append("none\n");
        }

        // Log SecurityContext
        try {
            org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            log.append("SecurityContext: ");
            if (auth != null && auth.isAuthenticated()) {
                log.append("principal=").append(auth.getName())
                   .append(", authorities=").append(auth.getAuthorities()).append("\n");
            } else {
                log.append("not authenticated\n");
            }
        } catch (Exception e) {
            log.append("error reading security context: ").append(e.getMessage()).append("\n");
        }

        // Log tenant context
        String tenantId = MultiTenantFilter.getCurrentTenantId();
        log.append("Tenant: ").append(tenantId != null ? tenantId : "none").append("\n");

        log.append("===").append(requestId).append("=============================");

        logger.info(log.toString());
    }

    private void logResponse(CachedBodyHttpServletResponse response, String requestId, long duration) {
        StringBuilder log = new StringBuilder();
        log.append("\n");
        log.append("=== ").append(requestId).append(" OUTGOING RESPONSE ===\n");
        log.append("Status: ").append(response.getStatus()).append("\n");
        log.append("Duration: ").append(duration).append("ms\n");

        // Log headers
        log.append("Headers:\n");
        for (String headerName : response.getHeaderNames()) {
            log.append("  ").append(headerName).append(": ").append(response.getHeader(headerName)).append("\n");
        }

        // Check if response is gzip compressed
        String contentEncoding = response.getHeader("Content-Encoding");
        boolean isGzipped = contentEncoding != null && contentEncoding.contains("gzip");

        // Log response body
        String responseBody = response.getCachedBodyString();
        if (isGzipped) {
            log.append("Response Body: (gzip compressed, unable to log)\n");
        } else if (!responseBody.isEmpty()) {
            // Try to format JSON if possible
            String formattedBody = responseBody;
            if (responseBody.startsWith("{") || responseBody.startsWith("[")) {
                // It's JSON - keep it formatted
                formattedBody = responseBody.substring(0, Math.min(1000, responseBody.length()));
                if (responseBody.length() > 1000) {
                    formattedBody += "... (truncated)";
                }
            } else {
                // Not JSON - truncate
                formattedBody = responseBody.substring(0, Math.min(200, responseBody.length()));
                if (responseBody.length() > 200) {
                    formattedBody += "... (truncated)";
                }
            }
            log.append("Response Body: ").append(formattedBody).append("\n");
        } else {
            log.append("Response Body: (empty)\n");
        }

        log.append("===").append(requestId).append("=============================");

        logger.info(log.toString());

        // IMPORTANT: Copy the captured response body back to the original response
        // This ensures the client receives the response data
        try {
            byte[] cachedBody = response.getCachedBody();
            if (cachedBody.length > 0) {
                response.copyBodyToResponse();
            }
        } catch (IOException e) {
            logger.error("Failed to copy response body: {}", e.getMessage());
        }
    }

    private String maskToken(String token) {
        if (token == null || token.isEmpty()) {
            return "none";
        }
        if (token.startsWith("Bearer ")) {
            return "Bearer " + token.substring(7, Math.min(20, token.length())) + "...";
        }
        return token.substring(0, Math.min(10, token.length())) + "...";
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip logging for actuator health checks to reduce noise
        String path = request.getRequestURI();
        return path.contains("/actuator/health");
    }
}
