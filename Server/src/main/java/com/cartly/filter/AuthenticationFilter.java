package com.cartly.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

@WebFilter("/api/*")
public class AuthenticationFilter implements Filter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());
        String method = httpRequest.getMethod();

        if (isPublicEndpoint(path, method)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        boolean isAuthenticated = (session != null && session.getAttribute("userId") != null);

        if (!isAuthenticated) {
            sendUnauthorizedResponse(httpResponse);
            return;
        }

        if (isAdminEndpoint(path)) {

            Object role = session.getAttribute("role");

            if (!"ADMIN".equals(role)) {
                sendForbiddenResponse(httpResponse);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path, String method) {
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        if (!"POST".equalsIgnoreCase(method)) {
            return false;
        }
        return "/api/auth/register".equals(path) || "/api/auth/login".equals(path);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> errorResponse = Map.of(
            "error", "Unauthorized",
            "message", "Authentication required. Please log in to access this resource."
        );

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    @Override
    public void destroy() {
    }
    
    private boolean isAdminEndpoint(String path) {
        return path.startsWith("/api/admin/");
    }

    private void sendForbiddenResponse(HttpServletResponse response)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> errorResponse = Map.of(
                "error", "Forbidden",
                "message", "Admin access required."
        );

        objectMapper.writeValue(
                response.getWriter(),
                errorResponse
        );
    }
}
