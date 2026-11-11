package dev.fitqueue.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.fitqueue.model.Booking;
import dev.fitqueue.model.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    long countByGymClassIdAndStatus(Long classId, BookingStatus status);

    boolean existsByGymClassIdAndMemberId(Long classId, Long memberId);
}
