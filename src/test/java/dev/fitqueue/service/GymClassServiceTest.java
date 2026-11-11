package dev.fitqueue.service;

import org.junit.jupiter.api.Test;


import dev.fitqueue.model.GymClass;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.fitqueue.dto.CreateGymClassRequest;

import dev.fitqueue.repository.GymClassRepository;

import static org.mockito.Mockito.mock;

import java.util.Optional;
import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class GymClassServiceTest {

    @Test
    void shouldFindClassById() {
        GymClassRepository repository = mock(GymClassRepository.class);

        GymClassService service = new GymClassService(repository);

        GymClass existingClass = new GymClass(2L, "Up training", 5);

        when(repository.findById(2L))
            .thenReturn(Optional.of(existingClass));

        GymClass gymClass = service.getClassById(2L);

        assertEquals("Up training", gymClass.getTitle());
    }

    @Test
    void shouldThrowWhenClassDoesNotExist() {
        GymClassRepository repository = mock(GymClassRepository.class);

        GymClassService service = new GymClassService(repository);

        when(repository.findById(99L))
            .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.getClassById(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test 
    void shouldCreateClass(){
        GymClassRepository repository = mock(GymClassRepository.class);

        GymClassService service = new GymClassService(repository);

        CreateGymClassRequest request = new CreateGymClassRequest("Yoga", 12);

        when(repository.save(any(GymClass.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        GymClass gymClass = service.createClass(request);

        assertEquals("Yoga", gymClass.getTitle());
        assertEquals(12, gymClass.getCapacity());

        verify(repository).save(gymClass);
    }
}