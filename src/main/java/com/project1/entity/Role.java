package com.project1.entity;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "role") 
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String role;
    @ManyToOne
    @JoinColumn(name = "worker_id")
    @JsonIgnore
    private Worker worker;


    public Role(String role) {
        this.role = role;
    }

}
