package com.project1.util;

import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    
    private final String secretKey;
    private final long expirationTime;
    private static final Set<String> blacklist = ConcurrentHashMap.newKeySet();
    
    public JwtUtil(@Value("${jwt.secret}") String secretKey, @Value("${jwt.expiration}") Long expirationTime) {
        this.secretKey = secretKey;
        this.expirationTime = expirationTime;
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(Long id, String role) {
        return Jwts.builder()
            .setSubject(String.valueOf(id))
            .claim("role", role)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public Long getWorkerId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    public String getRole(String token) {

        return getClaims(token).get("role", String.class);
    }

    public void clearTokenBlackList() {

        blacklist.clear();
    }

    public boolean validateToken(String token) {
        try {
            return !blacklist.contains(token) && getClaims(token) != null;

        } catch (Exception e) {

            return false;
        }
    }
    

    public void invalidateToken(String token) {
        
        blacklist.add(token);
    }
}