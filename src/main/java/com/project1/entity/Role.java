package com.project1.entity;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

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



    @Override
    public String toString() {
        return "Role{" +
               "id=" + id +
               ", role='" + role + '\'' +
               ", workerId=" + (worker != null ? worker.getId() : "null") +
               '}';
    }
}
