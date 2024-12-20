package com.project1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project1.entity.Reimbursement;
import com.project1.entity.Worker;

import java.util.List;



    
@Repository
public interface ReimbursementRepository extends JpaRepository <Reimbursement, Long> {
    List<Reimbursement> findByWorker(Worker worker);
    List<Reimbursement> findByWorkerAndStatus(Worker worker, String status);
    List<Reimbursement> findByStatus(String status);

    
}