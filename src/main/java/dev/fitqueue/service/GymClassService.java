package dev.fitqueue.service;

import java.util.List;

import org.springframework.stereotype.Service;

import dev.fitqueue.model.GymClass;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

import dev.fitqueue.dto.CreateGymClassRequest;

@Service 
public class GymClassService {

    private final List<GymClass> classes = new ArrayList<>(List.of(
        new GymClass(2L, "Up training", 5),
        new GymClass(3L, "Down training", 15)
    ));

    private long nextId = 4L;
    
    public List<GymClass> getClasses(){
        return List.copyOf(classes);
    }

    public GymClass getClassById(Long id){
        List<GymClass> list = getClasses();
        for(GymClass gymClass : list){
            if(gymClass.getId().equals(id)){
                return gymClass;
            }
        }

        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Gym class not found with id: " + id
        );
    }

    public GymClass createClass(CreateGymClassRequest request){
        GymClass gymClass = new GymClass(nextId ++, request.title(), request.capacity());

        classes.add(gymClass);
        
        return gymClass;
    }
}
