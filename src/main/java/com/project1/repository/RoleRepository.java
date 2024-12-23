package com.project1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project1.entity.Role;
import com.project1.entity.Worker;

@Repository
public interface RoleRepository extends JpaRepository <Role, Long> {
        List<Role> findByWorker(Worker worker);
    
}