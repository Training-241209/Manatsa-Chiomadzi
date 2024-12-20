package com.project1.entity;

import lombok.*;


import jakarta.persistence.*;

@Entity
@Table(name = "reimbursement") 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reimbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long amount;

    private String status = "pending";

    @ManyToOne
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    public Reimbursement(Worker worker, long amount) {
        this.amount = amount;
        this.worker = worker;
    }

    @Override
    public String toString() {
        return "Reimbursement{" +
               "id=" + id +
               ", amount=" + amount +
               ", status='" + status + '\'' +
               ", workerId=" + (worker != null ? worker.getId() : "null") +
               '}';
    }
}
