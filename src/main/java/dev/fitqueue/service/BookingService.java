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
import java.util.ArrayList;
import java.util.List;

import dev.fitqueue.model.BookingStatus;
import dev.fitqueue.dto.BookingResponse;

import java.time.Clock;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final GymClassRepository gymClassRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public BookingService(BookingRepository bookingRepository, GymClassRepository gymClassRepository, MemberRepository memberRepository, Clock clock){
        this.bookingRepository = bookingRepository;
        this.gymClassRepository = gymClassRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
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

        if(!gymClass.getStartsAt().isAfter(Instant.now(clock))) {
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
            Instant.now(clock)
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

        if(!Instant.now(clock).isBefore(cancellationDeadline)){
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

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsForClass(Long classId){
        if (!gymClassRepository.existsById(classId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Gym class not found with id: " + classId
            );
        }

        List<Booking> bookings = 
            bookingRepository.findByGymClassIdOrderByCreatedAtAscIdAsc(classId);

        List<BookingResponse> responses = new ArrayList<>();

        for (Booking booking : bookings) {
            responses.add(new BookingResponse(
                booking.getId(),
                booking.getGymClass().getId(),
                booking.getMember().getId(),
                booking.getBookingStatus(),
                booking.getCreatedAt()
            ));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsForMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Member not found with id: " + memberId
            );
        }

        List<Booking> bookings = 
            bookingRepository.findByMemberIdOrderByCreatedAtDescIdDesc(memberId);

        List<BookingResponse> responses = new ArrayList<>();

        for(Booking booking : bookings) {
            responses.add(new BookingResponse(
                booking.getId(),
                booking.getGymClass().getId(),
                booking.getMember().getId(),
                booking.getBookingStatus(),
                booking.getCreatedAt()
            ));
        }

        return responses;
    }
}