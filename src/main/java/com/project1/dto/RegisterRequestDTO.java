package com.project1.dto;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    
}
