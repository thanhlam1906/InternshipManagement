package com.example.internshipmanagement.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * HTTP Request/Response Logging Filter.
 *
 * Chức năng:
 * - Tạo Correlation ID (UUID) cho mỗi request để trace xuyên suốt hệ thống
 * - Gắn context (correlationId, clientIp) vào MDC cho tất cả log trong request
 * - Log thông tin request đến và response trả về
 * - Trả Correlation ID qua response header X-Correlation-Id
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String MDC_CORRELATION_ID = "correlationId";
    private static final String MDC_CLIENT_IP = "clientIp";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        // Tạo hoặc lấy Correlation ID từ header (cho phép client truyền vào)
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }

        // Gắn context vào MDC
        MDC.put(MDC_CORRELATION_ID, correlationId);
        MDC.put(MDC_CLIENT_IP, getClientIp(request));

        // Set Correlation ID vào response header
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        // Log request đến
        String queryString = request.getQueryString();
        String fullPath = queryString != null
                ? request.getRequestURI() + "?" + queryString
                : request.getRequestURI();

        log.info(">>> {} {} from {}", request.getMethod(), fullPath, getClientIp(request));

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log response
            log.info("<<< {} {} | Status: {} | Duration: {}ms",
                    request.getMethod(), request.getRequestURI(),
                    response.getStatus(), duration);

            // Clear MDC để tránh memory leak trong thread pool
            MDC.clear();
        }
    }

    /**
     * Lấy IP thực của client, hỗ trợ proxy/load balancer.
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            // X-Forwarded-For có thể chứa nhiều IP, lấy IP đầu tiên
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // Không log các request tới static resources
        String path = request.getRequestURI();
        return path.startsWith("/favicon.ico")
                || path.startsWith("/static/")
                || path.startsWith("/webjars/");
    }
}
