package com.project1.controller;

import org.springframework.web.bind.annotation.RestController;

import com.project1.entity.Reimbursement;

import com.project1.entity.Worker;
import com.project1.service.JwtService;
import com.project1.service.ReimbursementService;
import com.project1.service.WorkerService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Autowired
     private JwtService jwtService;

     
     public EmployeeController(WorkerService workerService) {
         this.workerService = workerService;
     }

     private boolean isAuthorized(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
    
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return false; 
        }
    
        String token = authorizationHeader.substring(7);
    
        try {
            List<Map<String, Object>> roles = jwtService.getRolesFromToken(token);
    
            return roles.stream()
                    .anyMatch(role -> "employee".equalsIgnoreCase((String) role.get("role")));
        } catch (Exception e) {
    
            return false;
        }
    }
    

     @GetMapping("/reimbursements/{workerId}")
     public ResponseEntity<?> getAllWorkerReimbursements(@PathVariable Long workerId, HttpServletRequest request) {

        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("denied: User role required.");
        }


         Worker worker = workerService.findWorkerById(workerId);

         List<Reimbursement> reimbursements = reimbService.getAllWorkerReimbursements(worker);
         return ResponseEntity.ok(reimbursements);
     }

     @GetMapping("/reimbursements/{workerId}/pending")
     public ResponseEntity<?> getAllWorkerPendingReimb(@PathVariable Long workerId, HttpServletRequest request) {

        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("denied: User role required.");
        }


         Worker worker = workerService.findWorkerById(workerId);

         if (!workerService.workerExists(worker)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
         List<Reimbursement> reimbursements = reimbService.getAllPendingReimb(worker);
         return ResponseEntity.ok(reimbursements);
     }


    @PostMapping("/reimbursement")
    public ResponseEntity<?> addReimbursementToWorker(@RequestBody Reimbursement reimb, HttpServletRequest request) {

        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("denied: User role required.");
        }

        try {
            if (reimb.getWorker()== null || reimb.getWorker().getId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Worker ID ");
            }
    
            Worker worker = workerService.findWorkerById(reimb.getWorker().getId());
            
            if (worker == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("invalid Worker ID " + reimb.getWorker().getId());
            }


            reimb.setWorker(worker);
            reimbService.persistReimbursement(reimb);
    
            return ResponseEntity.status(HttpStatus.CREATED).body(reimb);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
    
}
