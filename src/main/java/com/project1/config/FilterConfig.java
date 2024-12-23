package com.project1.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.project1.filter.JwtAuthenticationFilter;
import com.project1.util.JwtUtil;

@Configuration
public class FilterConfig {
    private final JwtUtil jwtUtil;

    public FilterConfig(JwtUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration() {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JwtAuthenticationFilter(jwtUtil));
        registrationBean.addUrlPatterns("/api/employee/*", "/api/manager/*");
        return registrationBean;
    }
}
