package dev.fitqueue.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import dev.fitqueue.repository.BookingRepository;
import dev.fitqueue.repository.GymClassRepository;
import dev.fitqueue.repository.MemberRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import dev.fitqueue.dto.BookingResponse;
import dev.fitqueue.model.Booking;
import dev.fitqueue.model.BookingStatus;
import dev.fitqueue.model.GymClass;
import dev.fitqueue.model.Member;

import static org.mockito.Mockito.when;

class BookingServiceTest {

    @Test
    void shouldConfirmBookingWhenSeatIsAvailable() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GymClassRepository gymClassRepository = mock(GymClassRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository);

        GymClass gymClass = new GymClass(
            1L, 
            "Yoga", 
            1, 
            Instant.now().plus(2, ChronoUnit.DAYS)
        );

        Member member = new Member(1L, "Maria", "maria@example.com");

        when(gymClassRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(gymClass));

        when(memberRepository.findById(1L))
            .thenReturn(Optional.of(member));

        when(bookingRepository.existsByGymClassIdAndMemberId(1L, 1L))
            .thenReturn(false);
        
        when(bookingRepository.countByGymClassIdAndStatus(
            1L, 
            BookingStatus.CONFIRMED))
                .thenReturn(0L);

        when(bookingRepository.save(any(Booking.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        BookingResponse response = service.createBooking(1L, 1L);

        assertEquals(BookingStatus.CONFIRMED, response.status());
        assertEquals(1L, response.classId());
        assertEquals(1L, response.memberId());
    }

    @Test
    void shouldWaitlistBookingWhenClassIsFull() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GymClassRepository gymClassRepository = mock(GymClassRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository);

        GymClass gymClass = new GymClass(
            1L, 
            "Yoga", 
            1, 
            Instant.now().plus(2, ChronoUnit.DAYS)
        );

        Member member = new Member(1L, "Maria", "maria@example.com");

        when(gymClassRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(gymClass));

        when(memberRepository.findById(1L))
            .thenReturn(Optional.of(member));

        when(bookingRepository.existsByGymClassIdAndMemberId(1L, 1L))
            .thenReturn(false);
        
        when(bookingRepository.countByGymClassIdAndStatus(
            1L, 
            BookingStatus.CONFIRMED))
                .thenReturn(1L);

        when(bookingRepository.save(any(Booking.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        BookingResponse response = service.createBooking(1L, 1L);

        assertEquals(BookingStatus.WAITLISTED, response.status());
        assertEquals(1L, response.classId());
        assertEquals(1L, response.memberId());
    }

    @Test 
    void shouldRejectDuplicateBooking(){
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GymClassRepository gymClassRepository = mock(GymClassRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository);

        GymClass gymClass = new GymClass(
            1L, 
            "Yoga", 
            1, 
            Instant.now().plus(2, ChronoUnit.DAYS)
        );

        Member member = new Member(1L, "Maria", "maria@example.com");

        when(gymClassRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(gymClass));

        when(memberRepository.findById(1L))
            .thenReturn(Optional.of(member));

        when(bookingRepository.existsByGymClassIdAndMemberId(1L, 1L))
            .thenReturn(true);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class, 
            () -> service.createBooking(1L, 1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void shouldRejectBookingWhenClassHasStarted(){
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GymClassRepository gymClassRepository = mock(GymClassRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository);

        GymClass gymClass = new GymClass(
            1L, 
            "Yoga", 
            1, 
            Instant.now().minus(1, ChronoUnit.HOURS)
        );

        Member member = new Member(1L, "Maria", "maria@example.com");

        when(gymClassRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(gymClass));

        when(memberRepository.findById(1L))
            .thenReturn(Optional.of(member));

        when(bookingRepository.existsByGymClassIdAndMemberId(1L, 1L))
            .thenReturn(false);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class, 
            () -> service.createBooking(1L, 1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(bookingRepository, never()).save(any(Booking.class));
    }
}