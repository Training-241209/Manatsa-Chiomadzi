package com.project1.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project1.entity.Role;
import com.project1.repository.RoleRepository;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    
    public Role persistRole(Role role){
        return roleRepository.save(role);
    }  
    
    public void deleteAllRoles() {
        roleRepository.deleteAll();
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }



}
