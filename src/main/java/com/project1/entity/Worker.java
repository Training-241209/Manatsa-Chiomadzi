package com.project1.entity;

import lombok.*;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "worker")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "worker")
@AllArgsConstructor
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String username;

    @JsonIgnoreProperties
    private String password;

    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL, orphanRemoval = true)

    private List<Role> roles = new ArrayList<>();

    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL, orphanRemoval = true)

    private List<Reimbursement> reimbursements = new ArrayList<>();

    public Worker(String firstName, String lastName, String username, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void addReimbursement(Reimbursement reimbursement) {
        this.reimbursements.add(reimbursement);
    }

}
