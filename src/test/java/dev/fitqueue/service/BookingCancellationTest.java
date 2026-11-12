package dev.fitqueue.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import dev.fitqueue.model.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import dev.fitqueue.dto.BookingResponse;
import dev.fitqueue.model.BookingStatus;
import dev.fitqueue.model.GymClass;
import dev.fitqueue.repository.BookingRepository;
import dev.fitqueue.repository.GymClassRepository;
import dev.fitqueue.repository.MemberRepository;

import dev.fitqueue.model.Booking;

@SpringBootTest 
@Testcontainers 
public class BookingCancellationTest {
    

    @Container 
    @ServiceConnection 
    static PostgreSQLContainer postgres = 
        new PostgreSQLContainer("postgres:17-alpine");

    @Autowired 
    BookingService bookingService;

    @Autowired 
    GymClassRepository gymClassRepository;

    @Autowired 
    MemberRepository memberRepository;

    @Autowired 
    BookingRepository bookingRepository;

    @Test 
    void shouldPersistCancellationAndWaitlistPromotion(){
        GymClass gymClass = gymClassRepository.save(
            new GymClass(
                null, 
                "Yoga", 
                1, 
                Instant.now().plus(2, ChronoUnit.DAYS)
            )
        );
        
        Member firstMember = memberRepository.save(
            new Member(null, "Maria", "maria-cancel@example.com")
        );

        Member secondMember = memberRepository.save(
            new Member(null, "Vaggelis", "vaggelis-cancel@example.com")
        );

        Member thirdMember = memberRepository.save(
            new Member(null, "Nikos", "nikos-cancel@example.com")
        );

        BookingResponse firstBooking = bookingService.createBooking(
            gymClass.getId(), firstMember.getId()
        );

        BookingResponse secondBooking = bookingService.createBooking(
            gymClass.getId(), secondMember.getId()
        );

        BookingResponse thirdBooking = bookingService.createBooking(
            gymClass.getId(), thirdMember.getId()
        );

        assertEquals(BookingStatus.CONFIRMED, firstBooking.status());
        assertEquals(BookingStatus.WAITLISTED, secondBooking.status());

        bookingService.cancelBooking(
            gymClass.getId(), firstMember.getId()
        );

        Booking cancelledBooking = bookingRepository
            .findById(firstBooking.id())
            .orElseThrow();

        Booking promotedBooking = bookingRepository
            .findById(secondBooking.id())
            .orElseThrow();

        assertEquals(
            BookingStatus.CANCELLED, 
            cancelledBooking.getBookingStatus()
        );

        assertEquals(
            BookingStatus.CONFIRMED, 
            promotedBooking.getBookingStatus()
        );

        Booking remainBooking = bookingRepository
            .findById(thirdBooking.id())
            .orElseThrow();

        assertEquals(
            BookingStatus.WAITLISTED, 
            remainBooking.getBookingStatus()
        );
    }
}
