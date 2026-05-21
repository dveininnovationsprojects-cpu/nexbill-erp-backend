package com.example.billing_backend.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String jsonError = String.format(
                "{\"error\": \"Unauthorized\", \"message\": \"%s\", \"path\": \"%s\"}",
                "Authentication is required to access this resource. Please provide a valid token.",
                request.getRequestURI()
        );

        response.getWriter().write(jsonError);
    }
}