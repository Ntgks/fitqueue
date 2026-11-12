package dev.fitqueue.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.fitqueue.model.Booking;
import dev.fitqueue.model.BookingStatus;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    long countByGymClassIdAndStatus(Long classId, BookingStatus status);

    boolean existsByGymClassIdAndMemberId(Long classId, Long memberId);

    Optional<Booking> findByGymClassIdAndMemberId(
        Long classId, Long memberId
    );

    Optional<Booking> findFirstByGymClassIdAndStatusOrderByCreatedAtAscIdAsc(
        Long classId, BookingStatus status
    );

    List<Booking> findByGymClassIdOrderByCreatedAtAscIdAsc(Long classId);

    List<Booking> findByMemberIdOrderByCreatedAtDescIdDesc(Long memberId);
}
