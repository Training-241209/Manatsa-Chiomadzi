package com.project1.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import com.project1.util.JwtUtil;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

public class JwtAuthenticationFilter implements Filter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, FilterChain chain) 
        throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String authorizationHeader = httpRequest.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Missing or invalid Authorization header");
            return;
        }

        String token = authorizationHeader.substring(7);

        if (!jwtUtil.validateToken(token)) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Token is invalid or has been blacklisted");
            return;
        }



        try {
            String role = jwtUtil.getRole(token);
            String requestURI = httpRequest.getRequestURI();

            if (requestURI.startsWith("/api/employee") && !"employee".equalsIgnoreCase(role)) {
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.getWriter().write("Access Denied: Employee role required");
                return;
            }

            if (requestURI.startsWith("/api/manager") && !"manager".equalsIgnoreCase(role)) {
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.getWriter().write("Access Denied: Manager role required");
                return;
            }

            httpRequest.setAttribute("userId", jwtUtil.getWorkerId(token));
            httpRequest.setAttribute("role", role);

            // String out = (String) httpRequest.getAttribute("userId");

            // System.out.println(out);

            chain.doFilter(request, response);
        } catch (Exception e) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Token is invalid or expired");

     
        }
    }
}
