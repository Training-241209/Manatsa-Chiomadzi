package com.project1.service;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project1.entity.Worker;
import com.project1.repository.WorkerRepository;

@Service
public class WorkerService {
    @Autowired
    private WorkerRepository workerRepository;

    public Worker findWorkerById(Long id) {
        return workerRepository.findById(id).orElse(null);
    }
    public Worker persistWorker(Worker worker){
        return workerRepository.save(worker);
    }  

    public Worker updateWorker(Worker worker){
        return workerRepository.save(worker);
    }  

    public List<Worker> getAllWorkers() {
        return workerRepository.findAll();
    }

    public boolean workerExists(Worker account) {
      if(account.getId() == null) return false;

      return workerRepository.existsById(account.getId());
    }

    public void deleteAllWorkers() {
        workerRepository.deleteAll();;
    }
    public void deleteWorkerById(Long id) {
        workerRepository.deleteById(id);

    }

    public Optional<Worker> findWorkerByUsername(String username) {
        return workerRepository.findByUsername(username);
    }

    
}
