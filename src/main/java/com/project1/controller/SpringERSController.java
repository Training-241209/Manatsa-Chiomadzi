package com.project1.controller;

import org.springframework.web.bind.annotation.RestController;

import com.project1.entity.Reimbursement;
import com.project1.entity.Role;
import com.project1.entity.Worker;
import com.project1.service.JwtService;
import com.project1.service.ReimbursementService;
import com.project1.service.RoleService;
import com.project1.service.WorkerService;

import com.project1.util.HelperUtil;

import java.util.List;
import java.util.Optional;

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
@RequestMapping("/api")

public class SpringERSController {

     @Autowired
     private WorkerService workerService;

     @Autowired
     private ReimbursementService reimbService;

     @Autowired
     private RoleService roleService;

     @Autowired
     private JwtService jwtService;

     private HelperUtil helperUtil = new HelperUtil();
     
     public SpringERSController(WorkerService workerService) {
         this.workerService = workerService;
     }

    @GetMapping
    public String welcome() {
        return "welcome to spring ERS api";
    }

    @GetMapping("/workers")
     public ResponseEntity<List<Worker>> getAllWorkers() {
         List<Worker> workers = workerService.getAllWorkers();
         return ResponseEntity.ok(workers);
     }

     @GetMapping("/roles")
     public ResponseEntity<List<Role>> getAllroles() {
         List<Role> roles = roleService.getAllRoles();
         return ResponseEntity.ok(roles);
     }

     @GetMapping("/reimbursements")
     public ResponseEntity<List<Reimbursement>> getAllreimbursements() {
         List<Reimbursement> reimbursements = reimbService.getAllReimbursements();
         return ResponseEntity.ok(reimbursements);
     }

     @GetMapping("/reimbursements/pending")
     public ResponseEntity<List<Reimbursement>> getAllPendingReimbursements() {
         List<Reimbursement> reimbursements = reimbService.getAllPendingReimbursements();
         return ResponseEntity.ok(reimbursements);
     }


     @GetMapping("/reimbursements/{workerId}")
     public ResponseEntity<List<Reimbursement>> getAllWorkerReimbursements(@PathVariable Long workerId) {

         Worker worker = workerService.findWorkerById(workerId);

         List<Reimbursement> reimbursements = reimbService.getAllWorkerReimbursements(worker);
         return ResponseEntity.ok(reimbursements);
     }

     @GetMapping("/reimbursements/{workerId}/pending")
     public ResponseEntity<List<Reimbursement>> getAllWorkerPendingReimb(@PathVariable Long workerId) {


         Worker worker = workerService.findWorkerById(workerId);

         if (!workerService.workerExists(worker)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
         List<Reimbursement> reimbursements = reimbService.getAllPendingReimb(worker);
         return ResponseEntity.ok(reimbursements);
     }

     
     @PostMapping("/login")
     public ResponseEntity<?> login(@RequestBody Worker worker) {
         try {
             String username = worker.getUsername(); 
             String password = worker.getPassword();
             String token;


             if(!helperUtil.isValidUsername(username)){
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Enter Valid email address");
             }
     
             Optional<Worker> workerOpt = workerService.findWorkerByUsername(username);

             if (!workerOpt.isPresent()) {
                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account not found");
             }
             

             Worker retWorker = workerOpt.get();

  
             if (helperUtil.verifyHash(password, retWorker.getPassword())) {
                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid password.");
             }

             token = jwtService.generateToken(retWorker);
             System.out.println(token);

            
             return ResponseEntity.status(HttpStatus.OK).body(helperUtil.tokenResponseJson(token));
     
         } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login Failed " + e.getMessage());
         }
     }
     

     @PostMapping("/register")
     public ResponseEntity<?> registerWorker(@RequestBody Worker worker) {
        try {

     

            if(!helperUtil.isValidUsername(worker.getUsername())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid email address");
            }

            if(worker.getPassword().isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid password");
            }


            Optional <Worker> workerOpt = workerService.findWorkerByUsername(worker.getUsername());

            if(workerOpt.isPresent()){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username Exists");          
            }

            System.out.println(worker.getPassword());

            worker.setPassword(helperUtil.hash(worker.getPassword())); //hash password

            Worker initialWorkerPersist = workerService.persistWorker(worker); 
            
            Role defRolePersit = new Role("employee");//add default emp role to user 
            defRolePersit.setWorker(initialWorkerPersist);
            Role roleWithGenWorkerId = roleService.persistRole(defRolePersit);

            worker.addRole(roleWithGenWorkerId);


            Worker newWorker = workerService.updateWorker(worker);
            
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(newWorker);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }


    @PostMapping("/reimbursement")
    public ResponseEntity<?> addReimbursementToWorker(@RequestBody Reimbursement reimb) {
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

    @PostMapping("/role")
    public ResponseEntity<?> addRole(@RequestBody Role role) {
        try {
            if (role.getWorker() == null || role.getWorker().getId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Worker ID ");
            }
    
            Worker worker = workerService.findWorkerById(role.getWorker().getId());
            if (worker == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("invalid Worker ID " + role.getWorker().getId());
            }

            role.setWorker(worker);
            roleService.persistRole(role);
    
            return ResponseEntity.status(HttpStatus.CREATED).body(role);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @PatchMapping("/reimbursements/{reimbId}/resolve")
    public ResponseEntity<?> resolveReimbursement(@PathVariable Long reimbId, @RequestBody Reimbursement reimbursement) {
        try {
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

   @DeleteMapping("/reimbursements/{id}")
   public ResponseEntity<String> deleteAllReimbursements(@PathVariable Long id) {
       try {
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
   @DeleteMapping("/reimbursements")
   public ResponseEntity<String> deleteAllReimbursements() {
       try {
           reimbService.deleteAllReimbursements();
           return ResponseEntity.status(HttpStatus.OK).body("All reimbursements deleted successfully");
       } catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting reimbursements: " + e.getMessage());
       }
   }

   @DeleteMapping("/workers/{id}")
    public ResponseEntity<String> deleteWorker(@PathVariable Long id) {
        try {
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
