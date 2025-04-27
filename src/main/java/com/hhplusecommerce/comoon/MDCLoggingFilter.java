package com.hhplusecommerce.comoon;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class MDCLoggingFilter implements Filter {

    private static final String REQUEST_ID = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String requestId = generateRequestId();
        setRequestIdToMDC(requestId);

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequestInfo(httpRequest, requestId, duration);
            clearMDC();
        }
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private void setRequestIdToMDC(String requestId) {
        MDC.put(REQUEST_ID, requestId);
    }

    private void clearMDC() {
        MDC.clear();
    }

    private void logRequestInfo(HttpServletRequest request, String requestId, long duration) {
        log.info("[{}] {} {} ({}ms)",
                requestId,
                request.getMethod(),
                request.getRequestURI(),
                duration
        );
    }
}
