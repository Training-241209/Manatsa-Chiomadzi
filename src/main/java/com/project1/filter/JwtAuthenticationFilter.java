package com.project1.filter;

import com.project1.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends HttpFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String authHeader = request.getHeader("Authorization");


        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); 

            try {

                Long workerId = jwtService.decodeToken(token); 
                List<Map<String, Object>> roles = jwtService.getRolesFromToken(token); 


                request.setAttribute("workerId", workerId);
                request.setAttribute("roles", roles);


                String path = request.getRequestURI();
                if (path.startsWith("/api/employee/") && !hasRole(roles, "employee")) {
                    sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Access denied: Employee role required.");
                    return;
                }
                if (path.startsWith("/api/manager/") && !hasRole(roles, "manager")) {
                    sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Access denied: Manager role required.");
                    return;
                }

            } catch (RuntimeException e) {

                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
                return;
            }
        }


        chain.doFilter(request, response);
    }


    private boolean hasRole(List<Map<String, Object>> roles, String roleName) {
        return roles.stream()
                .anyMatch(role -> roleName.equalsIgnoreCase((String) role.get("role")));
    }


    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
    }
}
