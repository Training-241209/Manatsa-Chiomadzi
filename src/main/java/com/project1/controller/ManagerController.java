package com.project1.controller;

import org.springframework.web.bind.annotation.RestController;

import com.project1.dto.AddRoleDTO;
import com.project1.dto.IdRequestDTO;
import com.project1.dto.UpdateStatusDTO;
import com.project1.entity.Reimbursement;
import com.project1.entity.Role;
import com.project1.entity.Worker;
import com.project1.service.ReimbursementService;
import com.project1.service.RoleService;
import com.project1.service.WorkerService;
import com.project1.util.HelperUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
     private RoleService roleService;

     private HelperUtil helperUtil = new HelperUtil();


     public ManagerController(WorkerService workerService) {
         this.workerService = workerService;
     }


     @GetMapping("/workers")
     public ResponseEntity<?>getAllWorkers(HttpServletRequest request) {

         List<Worker> workers = workerService.getAllWorkers();
         return ResponseEntity.ok(workers);
     }
     

     @GetMapping("/reimbursements")
     public ResponseEntity<?> getAllReimbursements(HttpServletRequest request) {

         List<Reimbursement> reimbursements = reimbService.getAllReimbursements();
         return ResponseEntity.ok(reimbursements);
     }

     @GetMapping("/reimbursements/pending")
     public ResponseEntity<?> getAllPendingReimbursements(HttpServletRequest request) {

         List<Reimbursement> reimbursements = reimbService.getAllPendingReimbursements();
         return ResponseEntity.ok(reimbursements);
     }

     @GetMapping("/reimbursements/{workerId}/pending")
     public ResponseEntity<?> getAllWorkerPendingReimb(@PathVariable Long workerId, HttpServletRequest request) {

         Worker worker = workerService.findWorkerById(workerId);

         if (!workerService.workerExists(worker)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
         List<Reimbursement> reimbursements = reimbService.getAllPendingReimb(worker);
         return ResponseEntity.ok(reimbursements);
     }
    @PostMapping("/role")
    public ResponseEntity<?> addRole(@RequestBody AddRoleDTO addRoleDTO) {
        try {

            addRoleDTO.getRole();
            String workerIdStr = String.valueOf(addRoleDTO.getWorkerId());


            if(addRoleDTO.getWorkerId() == null || String.valueOf(addRoleDTO.getWorkerId()).isEmpty() ){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing worker Id");
            }

           if (addRoleDTO.getRole() == null || addRoleDTO.getRole().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Role to add");
           }

           if(!helperUtil.isNumeric(workerIdStr)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("WorkerId must be numeric");
           }


           if (! (addRoleDTO.getRole().equalsIgnoreCase("manager") || addRoleDTO.getRole().equalsIgnoreCase("employee"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Role");
           }

            Worker worker = workerService.findWorkerById(addRoleDTO.getWorkerId());
            
            if (worker == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Worker not found" + addRoleDTO.getWorkerId());
            }


            Role defRolePersit = new Role(addRoleDTO.getRole());

            System.out.println(addRoleDTO.getRole());

            if(worker.getRoles().stream().anyMatch(r-> r.getRole().equalsIgnoreCase(addRoleDTO.getRole()))){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("role already exists");
            }

            defRolePersit.setWorker(worker);
            Role persistedRole = roleService.persistRole(defRolePersit);
            worker.addRole(persistedRole);

            workerService.updateWorker(worker);
    
            return ResponseEntity.status(HttpStatus.CREATED).body(persistedRole);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @PatchMapping("/reimbursement/resolve")
    public ResponseEntity<?> resolveReimbursement(@RequestBody UpdateStatusDTO updateStatusDTO, HttpServletRequest request) {
        try {


            String statusIdStr = String.valueOf(updateStatusDTO.getReimbId());   
            if(updateStatusDTO.getReimbId() == null || statusIdStr.isEmpty() ){
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Reimbursement Id");
            }

           if (updateStatusDTO.getStatus() == null || updateStatusDTO.getStatus().isEmpty()) {
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Status to add");
           }

           if(!helperUtil.isNumeric(statusIdStr)){
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("WorkerId must be numeric");
           }

            String status = updateStatusDTO.getStatus();
            if (!status.equalsIgnoreCase("approved") && !status.equalsIgnoreCase("denied")) {
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid status. Allowed values are 'approved' or 'denied'.");
            }


             Reimbursement  reimbExisting = reimbService.findReimbursementById(updateStatusDTO.getReimbId());

             if (reimbExisting  == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reimbursement not found with ID: " + updateStatusDTO.getReimbId());

             }

            reimbExisting.setStatus(status.toLowerCase());
            Reimbursement updatedReimbursement = reimbService.persistReimbursement(reimbExisting);
    
            return ResponseEntity.status(HttpStatus.OK).body(updatedReimbursement);
    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

   @DeleteMapping("/reimbursement")
   public ResponseEntity<String> deleteReimbursement(@RequestBody IdRequestDTO reimId) {
       try {

           Long id = reimId.getId();

           Reimbursement reimbOpt = reimbService.findReimbursementById(id);
           if (reimbOpt == null) {
               return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reimbursement not found with ID: " + id);
           }

           reimbService.deleteAllReimbursementsById(id);
           return ResponseEntity.status(HttpStatus.OK).body("Deleted Reimbursement " + id);
       } catch (Exception e) {
        
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting reimbursements: " + e.getMessage());
       }
   }


   @DeleteMapping("/worker")
    public ResponseEntity<String> deleteWorker(@RequestBody IdRequestDTO workerId) {
        try {

            Long id = workerId.getId();
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
    

    @DeleteMapping("/workers") 
    public ResponseEntity<String> deleteAllWorkers() {
        try {
            workerService.deleteAllWorkers();
            return ResponseEntity.status(HttpStatus.OK).body("All workers deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed deleting workers: " + e.getMessage());
        }
    }
 
    @DeleteMapping("/roles")
    public ResponseEntity<String> deleteAllRoles() {
        try {
            roleService.deleteAllRoles();
            return ResponseEntity.status(HttpStatus.OK).body("All roles deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting roles: " + e.getMessage());
        }
    }
 
 
    @DeleteMapping("/reimbursements")
    public ResponseEntity<String> deleteAllReimbursements() {
        try {
            reimbService.deleteAllReimbursements();
 
            return ResponseEntity.status(HttpStatus.OK).body("All reimbursements deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting reimbursements: " + e.getMessage());
        }
    }

    


    
}
