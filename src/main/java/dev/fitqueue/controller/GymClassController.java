package dev.fitqueue.controller;

import org.springframework.web.bind.annotation.RestController;

import dev.fitqueue.model.GymClass;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import dev.fitqueue.service.GymClassService;

import dev.fitqueue.dto.CreateGymClassRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;


@RestController 
public class GymClassController {

    private final GymClassService gymClassService;

    public GymClassController(GymClassService gymClassService){
        this.gymClassService = gymClassService;
    }
    
    @GetMapping("/api/classes/example")
    public GymClass getClassExample(){
        return new GymClass(1L, "Strength Training", 10);
    }

    @GetMapping("/api/classes")
    public List<GymClass> getClasses(){
        return gymClassService.getClasses();
    }

    @GetMapping("/api/classes/{id}")
    public GymClass getClassById(@PathVariable Long id){
        return gymClassService.getClassById(id);
    }

    @PostMapping("/api/classes")
    @ResponseStatus(HttpStatus.CREATED)
    public GymClass createClass(@Valid @RequestBody CreateGymClassRequest request) {
        return gymClassService.createClass(request);
    }
    
    
}
