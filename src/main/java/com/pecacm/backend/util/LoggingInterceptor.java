package com.pecacm.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        logger.info("Received request: {} {} Authorization={}", request.getMethod(), request.getRequestURI(), request.getHeader("Authorization"));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if(ex == null) {
            logger.info("Sent response: {} {} with status {}", request.getMethod(), request.getRequestURI(), response.getStatus());
        }
        else {
            logger.error("Sent response: {} {} with status {} and exception {}", request.getMethod(), request.getRequestURI(), response.getStatus(), ex.getStackTrace());
        }
    }
}