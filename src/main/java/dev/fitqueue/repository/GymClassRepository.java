package dev.fitqueue.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.fitqueue.model.GymClass;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GymClassRepository extends JpaRepository<GymClass, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from GymClass g where g.id = :id")
    Optional<GymClass> findByIdForUpdate(@Param("id") Long id);
}
