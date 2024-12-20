package com.project1.service;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;
import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project1.entity.Worker;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtService {

    @Value("${jwt.secret.key}") 
    private String secretKey;

    public String generateToken(Worker worker){
        return Jwts.builder()
            .claim("id", worker.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) 
            .signWith(getSigningKey())
            .compact();

    }

    public Long decodeToken(String token) {

        try{
            var claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
             
            return claims.get("id", Long.class);

        } catch (ExpiredJwtException e){
            throw new RuntimeErrorException(null, "Token is Expired");
        }catch (JwtException e){
            throw new RuntimeErrorException(null,"Token in Invalid");
        }

    }

    public boolean isValidToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
    
    public List<Map<String, Object>> getRolesFromToken(String token) {
        try {
            var claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey()) 
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
    
            return claims.get("roles", List.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid token: " + e.getMessage());
        }
    }
    
    

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    

    
    
}
