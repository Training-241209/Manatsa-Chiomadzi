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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;



@RestController
@RequestMapping("/api/manager/")

public class ManagerController {

     @Autowired
     private WorkerService workerService;

     @Autowired
     private ReimbursementService reimbService;

     @Autowired
     private JwtService jwtService;

     public ManagerController(WorkerService workerService) {
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
    
            return roles.stream().anyMatch(role -> "manager".equalsIgnoreCase((String) role.get("role")));
            
        } catch (Exception e) {
    
            return false;
        }
    }
    

     @GetMapping("/workers")
     public ResponseEntity<?>getAllWorkers(HttpServletRequest request) {

        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            System.out.println(headerName + ": " + request.getHeader(headerName));
        });

        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Manager role required.");
        }
         List<Worker> workers = workerService.getAllWorkers();
         return ResponseEntity.ok(workers);
     }

     @GetMapping("/reimbursements/pending")
     public ResponseEntity<?> getAllPendingReimbursements(HttpServletRequest request) {

        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Manager role required.");
        }

         List<Reimbursement> reimbursements = reimbService.getAllPendingReimbursements();
         return ResponseEntity.ok(reimbursements);
     }

     @GetMapping("/reimbursements/{workerId}/pending")
     public ResponseEntity<?> getAllWorkerPendingReimb(@PathVariable Long workerId, HttpServletRequest request) {

        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Manager role required.");
        }


         Worker worker = workerService.findWorkerById(workerId);

         if (!workerService.workerExists(worker)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
         List<Reimbursement> reimbursements = reimbService.getAllPendingReimb(worker);
         return ResponseEntity.ok(reimbursements);
     }

    @PatchMapping("/reimbursements/{reimbId}/resolve")
    public ResponseEntity<?> resolveReimbursement(@PathVariable Long reimbId, @RequestBody Reimbursement reimbursement, HttpServletRequest request) {
        try {

            if (!isAuthorized(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Manager role required.");
            }

            String status = reimbursement.getStatus();
            if (status == null || (!status.equalsIgnoreCase("approved") && !status.equalsIgnoreCase("denied"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid status. Allowed values are 'approved' or 'denied'.");
            }


             Reimbursement  reimbExisting = reimbService.findReimbursementById(reimbId);

             if (reimbExisting  == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reimbursement not found with ID: " + reimbId);

             }

            reimbExisting.setStatus(status.toLowerCase());
            Reimbursement updatedReimbursement = reimbService.persistReimbursement(reimbExisting);
    
            return ResponseEntity.status(HttpStatus.OK).body(updatedReimbursement);
    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

   @DeleteMapping("/reimbursements/{id}")
   public ResponseEntity<String> deleteAllReimbursements(@PathVariable Long id, HttpServletRequest request) {
       try {

        if (!isAuthorized(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Manager role required.");
        }
           Reimbursement reimbOpt = reimbService.findReimbursementById(id);
           if (reimbOpt == null) {
               return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reimbursement not found with ID: " + id);
           }

           reimbService.deleteAllReimbursementsById(id);
           return ResponseEntity.status(HttpStatus.OK).body("All reimbursements deleted successfully");
       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting reimbursements: " + e.getMessage());
       }
   }

   @DeleteMapping("/workers/{id}")
    public ResponseEntity<String> deleteWorker(@PathVariable Long id, HttpServletRequest request) {
        try {

            if (!isAuthorized(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Manager role required.");
            }

            Worker workerOpt = workerService.findWorkerById(id);
            if (workerOpt == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Worker not found with ID: " + id);
            }
            workerService.deleteWorkerById(id);
            return ResponseEntity.status(HttpStatus.OK).body("Worker deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting worker: " + e.getMessage());
        }
    }

    


    
}
