package com.project1.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project1.entity.Worker;

@Repository
public interface WorkerRepository extends JpaRepository <Worker, Long> {
    Optional<Worker> findByUsername(String username);
    

    
}