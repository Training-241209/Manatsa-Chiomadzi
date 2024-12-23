package com.project1.controller;

import org.springframework.web.bind.annotation.RestController;

import com.project1.dto.ReimbRequestDTO;
import com.project1.entity.Reimbursement;

import com.project1.entity.Worker;
import com.project1.service.ReimbursementService;
import com.project1.service.WorkerService;
import com.project1.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;



@RestController
@RequestMapping("/api/employee/")

public class EmployeeController {



     @Autowired
     private WorkerService workerService;

     @Autowired
     private ReimbursementService reimbService;

     public EmployeeController(
            WorkerService workerService, JwtUtil jwtUtil) {
     }


     @GetMapping("/reimbursements")
     public ResponseEntity<?> getAllWorkerReimbursements(HttpServletRequest request) {

         Long workerId = Long.parseLong(String.valueOf(request.getAttribute("userId")));
         Worker worker = workerService.findWorkerById(workerId);

         List<Reimbursement> reimbursements = reimbService.getAllWorkerReimbursements(worker);
         return ResponseEntity.ok(reimbursements);
     }

     @GetMapping("/reimbursements/pending")
     public ResponseEntity<?> getAllWorkerPendingReimb(HttpServletRequest request) {

         Long workerId = Long.parseLong(String.valueOf(request.getAttribute("userId")));

         Worker worker = workerService.findWorkerById(workerId);

         if (!workerService.workerExists(worker)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
         List<Reimbursement> reimbursements = reimbService.getAllPendingReimb(worker);
         return ResponseEntity.ok(reimbursements);
     }


    @PostMapping("/reimbursement")
    public ResponseEntity<?> addReimbursementToWorker(@RequestBody ReimbRequestDTO reimbRequestDTO, HttpServletRequest request) {

        try {

            Long workerId = Long.parseLong(String.valueOf(request.getAttribute("userId")));
            Worker worker = workerService.findWorkerById(workerId);
            
            Reimbursement reimb = new Reimbursement();
            reimb.setWorker(worker);
            reimb.setAmount(reimbRequestDTO.getAmount());
            reimb.setDescription(reimbRequestDTO.getDescription());
        
            reimbService.persistReimbursement(reimb);
    
            return ResponseEntity.status(HttpStatus.CREATED).body(reimb);
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
    
}
