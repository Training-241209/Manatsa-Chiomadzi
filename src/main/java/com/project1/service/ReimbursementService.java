package com.project1.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project1.entity.Reimbursement;
import com.project1.entity.Worker;

import com.project1.repository.ReimbursementRepository;


@Service
public class ReimbursementService {

    @Autowired
    private ReimbursementRepository reimbRepository;


    public Reimbursement persistReimbursement(Reimbursement reimb) {
        return reimbRepository.save(reimb);
    }   

    public List<Reimbursement> getAllReimbursements() {
        return reimbRepository.findAll();
    }

    public void deleteAllReimbursements() {
        reimbRepository.deleteAll();
    }

    public List<Reimbursement> getAllWorkerReimbursements(Worker worker) {
        return reimbRepository.findByWorker(worker);
    }

    public List<Reimbursement> getAllPendingReimb(Worker worker) {
        return reimbRepository.findByWorkerAndStatus(worker, "pending");
    }

    public List<Reimbursement> getAllPendingReimbursements() {
        return reimbRepository.findByStatus("pending");
    }

    public Reimbursement findReimbursementById(Long reimbId) {
        return reimbRepository.findById(reimbId).orElse(null);
    }

    public void deleteAllReimbursementsById(Long id) {
        reimbRepository.deleteById(id);;
    }


}
