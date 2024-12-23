package com.project1.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project1.dto.AddRoleDTO;
import com.project1.dto.LoginRequestDTO;
import com.project1.dto.RegisterRequestDTO;
import com.project1.entity.Role;
import com.project1.entity.Worker;
import com.project1.service.RoleService;
import com.project1.service.WorkerService;
import com.project1.util.HelperUtil;
import com.project1.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping("api/auth/")
public class AuthController {

    
     private final WorkerService workerService;
     private final RoleService roleService;
     private final HelperUtil helperUtil;
     private final JwtUtil jwtUtil;
     
    //  @Autowired
    public AuthController(WorkerService workerService,RoleService roleService,JwtUtil jwtUtil) {
        this.workerService = workerService;
        this.roleService = roleService;
        this.helperUtil = new HelperUtil();
        this.jwtUtil = jwtUtil;
    }



     @PostMapping("/login")
     public ResponseEntity<?> login(@RequestBody LoginRequestDTO worker) {
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

            String workerRole =  "";
            if(retWorker.getRoles().stream().anyMatch(role -> "employee".equalsIgnoreCase(role.getRole()))){
                workerRole = "employee";       
            }

            if(retWorker.getRoles().stream().anyMatch(role -> "manager".equalsIgnoreCase(role.getRole()))){
                workerRole= "manager";       
            }

            jwtUtil.clearTokenBlackList();
            String token = jwtUtil.generateToken(retWorker.getId(), workerRole);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            
            return ResponseEntity.status(HttpStatus.OK).body(response);
     
         } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login Failed " + e.getMessage());
         }
     }

     @PostMapping("/logout")
     public ResponseEntity<?> logout(HttpServletRequest request) {
         String authorizationHeader = request.getHeader("Authorization");
 
         if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
             return ResponseEntity.badRequest().body("Missing or invalid Authorization header");
         }
 
         String token = authorizationHeader.substring(7);
 
         jwtUtil.invalidateToken(token);
 
         return ResponseEntity.ok("Logged out");
     }
     

     @PostMapping("/register")
     public ResponseEntity<?> registerWorker(@RequestBody RegisterRequestDTO registerRequestDTO) {    
        try {

            if(registerRequestDTO.getFirstName() == null || registerRequestDTO.getFirstName().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing first name");

            }

            if(registerRequestDTO.getLastName() == null || registerRequestDTO.getLastName().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing last name");

            }    

            if(registerRequestDTO.getUsername() == null || registerRequestDTO.getUsername().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing username");

            }   

            if(!helperUtil.isValidUsername(registerRequestDTO.getUsername())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid email address");

            }

            if(registerRequestDTO.getPassword() == null || registerRequestDTO.getPassword().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("missing password");

            }   

            Optional<Worker> workerOpt = workerService.findWorkerByUsername(registerRequestDTO.getUsername());
            if(workerOpt.isPresent()){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username Exists");          
            }

            Worker worker = new Worker();
            worker.setFirstName(registerRequestDTO.getFirstName());
            worker.setLastName(registerRequestDTO.getLastName());
            worker.setUsername(registerRequestDTO.getUsername());
            worker.setPassword(helperUtil.hash(registerRequestDTO.getPassword()));

            Worker initialWorkerPersist = workerService.persistWorker(worker); 
            
            Role defRolePersit = new Role("employee");//add default emp role to user 
            defRolePersit.setWorker(initialWorkerPersist);

            Role roleWithGenWorkerId = roleService.persistRole(defRolePersit);
            worker.addRole(roleWithGenWorkerId);


            workerService.updateWorker(worker);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Registration successful");

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }



    ///*****************************************************************Helper endpoints won't be in prod ******************** */

    @GetMapping("/workers")
    public ResponseEntity<?>getAllWorkers(HttpServletRequest request) {

        List<Worker> workers = workerService.getAllWorkers();
        return ResponseEntity.ok(workers);
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
}


