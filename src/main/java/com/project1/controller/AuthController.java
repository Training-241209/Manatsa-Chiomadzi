package com.project1.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project1.entity.Role;
import com.project1.entity.Worker;
import com.project1.service.JwtService;
import com.project1.service.RoleService;
import com.project1.service.WorkerService;
import com.project1.util.HelperUtil;


@RestController
@RequestMapping("api/auth/")
public class AuthController {


     private WorkerService workerService;

     private RoleService roleService;


     private JwtService jwtService;

     private HelperUtil helperUtil = new HelperUtil();
     
     @Autowired
     public AuthController(WorkerService workerService, RoleService roleService, JwtService jwtService) {
         this.workerService = workerService;
         this.roleService = roleService;
         this.jwtService = jwtService;
         this.helperUtil = new HelperUtil();
     }


     @PostMapping("/login")
     public ResponseEntity<?> login(@RequestBody Worker worker) {
         try {

             if(worker == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing credentials");              
             }

             String username = worker.getUsername(); 

             if(!helperUtil.isValidUsername(username)){
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Enter Valid email address");
             }
             if(worker.getPassword().isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Enter password");
            }
     
             Optional<Worker> workerOpt = workerService.findWorkerByUsername(username);

             if (!workerOpt.isPresent()) {
                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account not found");
             }
             

            Worker retWorker = workerOpt.get();

            String password = helperUtil.hash(worker.getPassword());
            if(!password.equals(retWorker.getPassword())){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid password.");
            }



             String token = jwtService.generateToken(retWorker);
             System.out.println(token);

            
             return ResponseEntity.status(HttpStatus.OK).body(Map.of("token", token,"workerId", retWorker.getId(),"roles", retWorker.getRoles()));
     
         } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login Failed " + e.getMessage());
         }
     }
     

     @PostMapping("/register")
     public ResponseEntity<?> registerWorker(@RequestBody Worker worker) {
        try {

            if(worker.getFirstName().isBlank()){
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing first name");
            }

            if(worker.getLastName().isBlank()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing last name");
            }    

            if(!helperUtil.isValidUsername(worker.getUsername())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid email address");
            }

            if(worker.getPassword().isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Enter password");
            }


            Optional<Worker> workerOpt = workerService.findWorkerByUsername(worker.getUsername());


            if(workerOpt.isPresent()){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username Exists");          
            }

        
            String password = helperUtil.hash(worker.getPassword());

            worker.setPassword(password); 
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
    
}

