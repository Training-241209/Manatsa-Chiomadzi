package com.project1.dto;

import lombok.Data;

@Data
public class IdRequestDTO {
    private Long id;
    
    public IdRequestDTO(Long id){
        this.id = id;
    }


    
}
