package dev.fitqueue.service;

import org.springframework.stereotype.Service;

import dev.fitqueue.repository.BookingRepository;

import dev.fitqueue.repository.GymClassRepository;
import dev.fitqueue.repository.MemberRepository;

import dev.fitqueue.model.Booking;
import dev.fitqueue.model.GymClass;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import dev.fitqueue.model.Member;

import java.time.Duration;
import java.time.Instant;
import dev.fitqueue.model.BookingStatus;
import dev.fitqueue.dto.BookingResponse;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final GymClassRepository gymClassRepository;
    private final MemberRepository memberRepository;

    public BookingService(BookingRepository bookingRepository, GymClassRepository gymClassRepository, MemberRepository memberRepository){
        this.bookingRepository = bookingRepository;
        this.gymClassRepository = gymClassRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public BookingResponse createBooking(Long classId, Long memberId){
        GymClass gymClass = gymClassRepository.findByIdForUpdate(classId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Gym class not found with id: " + classId
        ));

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Member not found with id: " + memberId
        ));

        if(bookingRepository.existsByGymClassIdAndMemberId(classId, memberId)){
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Member already has a booking for this class"
            );
        }

        if(!gymClass.getStartsAt().isAfter(Instant.now())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Cannot book a class that has already started"
            );
        }

        long confirmedBookings = bookingRepository.countByGymClassIdAndStatus(
            classId, 
            BookingStatus.CONFIRMED
        );

        BookingStatus status;

        if(confirmedBookings < gymClass.getCapacity()) {
            status = BookingStatus.CONFIRMED;
        } else {
            status = BookingStatus.WAITLISTED;
        }

        Booking booking = new Booking(
            null, 
            gymClass, 
            member, 
            status, 
            Instant.now()
        );

        Booking savedBooking = bookingRepository.save(booking);

        return new BookingResponse(
            savedBooking.getId(),
            gymClass.getId(),
            member.getId(),
            savedBooking.getBookingStatus(),
            savedBooking.getCreatedAt()
        );
    }

    @Transactional 
    public BookingResponse cancelBooking(Long classId, Long memberId){
        GymClass gymClass = gymClassRepository.findByIdForUpdate(classId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Gym class not found with id: " + classId
        ));

        Booking booking = bookingRepository
            .findByGymClassIdAndMemberId(classId, memberId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Booking not found"
        ));

        if(booking.getBookingStatus() == BookingStatus.CANCELLED){
            return new BookingResponse(
                booking.getId(),
                classId,
                memberId,
                booking.getBookingStatus(),
                booking.getCreatedAt()
            );
        }

        Instant cancellationDeadline = 
            gymClass.getStartsAt().minus(Duration.ofHours(2));

        if(!Instant.now().isBefore(cancellationDeadline)){
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Cancellation closes two hours before class starts"
            );
        }

        boolean releaseSeat = 
            booking.getBookingStatus() == BookingStatus.CONFIRMED;
        
        booking.cancel();

        if(releaseSeat){
            bookingRepository
                .findFirstByGymClassIdAndStatusOrderByCreatedAtAscIdAsc(
                    classId, 
                    BookingStatus.WAITLISTED
                )
                .ifPresent(nextBooking -> nextBooking.confirm());
        }

        return new BookingResponse(
            booking.getId(),
            classId,
            memberId,
            booking.getBookingStatus(),
            booking.getCreatedAt()
        );

    }
}