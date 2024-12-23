package com.project1.entity;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;

@Entity
@Table(name = "reimbursement") 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "worker")
public class Reimbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long amount;

    private String status = "pending";
    private String description;

    @ManyToOne
    @JoinColumn(name = "worker_id", nullable = false)
    @JsonIgnore
    private Worker worker;


    @JsonProperty("workerId")
    public Long getWorkerId() {
        return worker != null ? worker.getId() : null;
    }

    public Reimbursement(Worker worker, long amount, String description) {

        this.amount = amount;
        this.worker = worker;
        this.description = description;
    }


}
