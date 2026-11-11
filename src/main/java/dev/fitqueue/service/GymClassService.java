package dev.fitqueue.service;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.fitqueue.model.GymClass;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import dev.fitqueue.dto.CreateGymClassRequest;

import dev.fitqueue.repository.GymClassRepository;

@Service 
public class GymClassService {

    private final GymClassRepository gymClassRepository;

    public GymClassService(GymClassRepository gymClassRepository){
        this.gymClassRepository = gymClassRepository;
    }
    
    public List<GymClass> getClasses(){
        return gymClassRepository.findAll();
    }

    public GymClass getClassById(Long id){
        return gymClassRepository.findById(id)
            .orElseThrow( () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Gym class not found with id: " + id
            ));
    }

    public GymClass createClass(CreateGymClassRequest request){
        GymClass gymClass = new GymClass(
            null, 
            request.title(), 
            request.capacity()
        );

        return gymClassRepository.save(gymClass);
    }
}
