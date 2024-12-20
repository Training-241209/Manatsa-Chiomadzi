package com.project1.util;
import java.util.Map;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.beans.factory.annotation.Value;
import java.util.Base64;
import java.util.HashMap;


import java.util.regex.*;


public class HelperUtil {

    @Value("${encryption.hash.type}")
    private String encryptType;

    public boolean isNumeric(String s){
        for(char c : s.toCharArray()){
            if(Character.isDigit(c)) continue;
            return false;
        }
        return true;
    }


    public boolean isValidUsername(String username){
        if(username.isBlank())
            return false;
        
        if(!emailRegex(username))
            return false;

        return true;
        
    }

    public boolean emailRegex(String email){
        Pattern p = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
        return  p.matcher(email).matches();
    }


    public Map<String, String> tokenResponseJson(String accessToken) {
            Map<String, String> res = new HashMap<>();
            res.put("token", accessToken);
            return res;
    }

    public String hash(String password)  throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = md.digest(password.getBytes());
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    public boolean verifyHash(String password, String hash) throws NoSuchAlgorithmException {
            return hash.equals(hash(password));
    }


    
    
}