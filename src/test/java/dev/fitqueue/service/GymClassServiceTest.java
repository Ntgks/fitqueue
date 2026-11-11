package dev.fitqueue.service;

import org.junit.jupiter.api.Test;


import dev.fitqueue.model.GymClass;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GymClassServiceTest {

    @Test
    void shouldFindClassById() {
        GymClassService service = new GymClassService();

        GymClass gymClass = service.getClassById(2L);

        assertEquals("Up training", gymClass.getTitle());
    }

    @Test
    void shouldThrowWhenClassDoesNotExist() {
        GymClassService service = new GymClassService();

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.getClassById(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}