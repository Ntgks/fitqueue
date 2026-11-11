package dev.fitqueue.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.fitqueue.model.GymClass;

public interface GymClassRepository extends JpaRepository<GymClass, Long> {
}
