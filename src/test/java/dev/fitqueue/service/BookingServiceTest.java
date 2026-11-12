package dev.fitqueue.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import java.util.List;
import java.util.Optional;

import dev.fitqueue.dto.BookingResponse;
import dev.fitqueue.model.Booking;
import dev.fitqueue.model.BookingStatus;
import dev.fitqueue.model.GymClass;
import dev.fitqueue.model.Member;

import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.ZoneOffset;

class BookingServiceTest {

    private final Clock clock = Clock.fixed(
        Instant.parse("2030-06-15T12:00:00Z"), 
        ZoneOffset.UTC
    );

    @Test
    void shouldConfirmBookingWhenSeatIsAvailable() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GymClassRepository gymClassRepository = mock(GymClassRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository, clock);

        GymClass gymClass = new GymClass(
            1L, 
            "Yoga", 
            1, 
            Instant.now(clock).plus(2, ChronoUnit.DAYS)
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

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository, clock);

        GymClass gymClass = new GymClass(
            1L, 
            "Yoga", 
            1, 
            Instant.now(clock).plus(2, ChronoUnit.DAYS)
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

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository, clock);

        GymClass gymClass = new GymClass(
            1L, 
            "Yoga", 
            1, 
            Instant.now(clock).plus(2, ChronoUnit.DAYS)
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

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository, clock);

        GymClass gymClass = new GymClass(
            1L, 
            "Yoga", 
            1, 
            Instant.now(clock).minus(1, ChronoUnit.HOURS)
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

    @Test 
    void shouldPromoteWaitlistedBookingWhenConfirmedBookingIsCancelled() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GymClassRepository gymClassRepository = mock(GymClassRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository, clock);

        GymClass gymClass = new GymClass(
            1L, 
            "Yoga", 
            1, 
            Instant.now(clock).plus(2, ChronoUnit.DAYS)
        );

        Member memberOne = new Member(1L, "Maria", "maria@example.com");

        Member memberTwo = new Member(2L, "Vaggelis", "vaggelis@example.com");

        Booking confirmedBooking = new Booking(
            1L,
            gymClass,
            memberOne,
            BookingStatus.CONFIRMED,
            Instant.now(clock).minus(2, ChronoUnit.HOURS)
        );

        Booking waitlistedBooking = new Booking(
            2L,
            gymClass,
            memberTwo,
            BookingStatus.WAITLISTED,
            Instant.now(clock).minus(1, ChronoUnit.HOURS)
        );

        when(gymClassRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(gymClass));

        when(bookingRepository.findByGymClassIdAndMemberId(1L, 1L))
            .thenReturn(Optional.of(confirmedBooking));

        when(bookingRepository
                .findFirstByGymClassIdAndStatusOrderByCreatedAtAscIdAsc(
                    1L, BookingStatus.WAITLISTED))
            .thenReturn(Optional.of(waitlistedBooking));

        BookingResponse response = service.cancelBooking(1L, 1L);

        assertEquals(BookingStatus.CANCELLED, response.status());
        assertEquals(BookingStatus.CANCELLED, confirmedBooking.getBookingStatus());
        assertEquals(BookingStatus.CONFIRMED, waitlistedBooking.getBookingStatus());
    }

    @Test
    void shouldNotPromoteWhenWaitlistedBookingIsCancelled() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GymClassRepository gymClassRepository = mock(GymClassRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository, clock);

        GymClass gymClass = new GymClass(
            1L, 
            "Yoga", 
            1, 
            Instant.now(clock).plus(2, ChronoUnit.DAYS)
        );

        Member member = new Member(1L, "Maria", "maria@example.com");

        Booking waitlistedBooking = new Booking(
            2L,
            gymClass,
            member,
            BookingStatus.WAITLISTED,
            Instant.now(clock).minus(1, ChronoUnit.HOURS)
        );

        when(gymClassRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(gymClass));

        when(bookingRepository.findByGymClassIdAndMemberId(1L, 1L))
            .thenReturn(Optional.of(waitlistedBooking));

        BookingResponse response = service.cancelBooking(1L, 1L);

        assertEquals(BookingStatus.CANCELLED, response.status());

        verify(bookingRepository, never())
            .findFirstByGymClassIdAndStatusOrderByCreatedAtAscIdAsc(
                1L, BookingStatus.WAITLISTED
            );
    }

    @Test
    void shouldRejectCancellationLessThanTwoHoursBeforeStart() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GymClassRepository gymClassRepository = mock(GymClassRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository, clock);

        GymClass gymClass = new GymClass(
            1L, 
            "Yoga", 
            1, 
            Instant.now(clock).plus(1, ChronoUnit.HOURS)
        );

        Member member = new Member(1L, "Maria", "maria@example.com");

        Booking confirmedBooking = new Booking(
            2L,
            gymClass,
            member,
            BookingStatus.CONFIRMED,
            Instant.now(clock).minus(1, ChronoUnit.HOURS)
        );

        when(gymClassRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(gymClass));

        when(bookingRepository.findByGymClassIdAndMemberId(1L, 1L))
            .thenReturn(Optional.of(confirmedBooking));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class, 
            () -> service.cancelBooking(1L, 1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        assertEquals(
            BookingStatus.CONFIRMED,
            confirmedBooking.getBookingStatus()
        );

        verify(bookingRepository, never())
            .findFirstByGymClassIdAndStatusOrderByCreatedAtAscIdAsc(
                1L, BookingStatus.WAITLISTED
            );
    }

    @Test 
    void shouldReturnCancelledBookingWithoutPromotingAgain() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GymClassRepository gymClassRepository = mock(GymClassRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository, clock);

        GymClass gymClass = new GymClass(
            1L, 
            "Yoga", 
            1, 
            Instant.now(clock).plus(1, ChronoUnit.HOURS)
        );

        Member member = new Member(1L, "Maria", "maria@example.com");

        Booking cancelledBooking = new Booking(
            2L,
            gymClass,
            member,
            BookingStatus.CANCELLED,
            Instant.now(clock).minus(1, ChronoUnit.HOURS)
        );

        when(gymClassRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(gymClass));

        when(bookingRepository.findByGymClassIdAndMemberId(1L, 1L))
            .thenReturn(Optional.of(cancelledBooking));

        BookingResponse response = service.cancelBooking(1L, 1L);

        assertEquals(BookingStatus.CANCELLED, response.status());

        assertEquals(
            BookingStatus.CANCELLED,
            cancelledBooking.getBookingStatus()
        );

        verify(bookingRepository, never())
            .findFirstByGymClassIdAndStatusOrderByCreatedAtAscIdAsc(
                1L, BookingStatus.WAITLISTED
            );
    }

    @Test
    void shouldReturnEmptyListWhenClassHasNoBookings(){
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GymClassRepository gymClassRepository = mock(GymClassRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository, clock);

        when(gymClassRepository.existsById(1L)).thenReturn(true);

        when(bookingRepository.findByGymClassIdOrderByCreatedAtAscIdAsc(1L))
            .thenReturn(List.of());

        List<BookingResponse> result = service.getBookingsForClass(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectBookingListForMissingClass() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GymClassRepository gymClassRepository = mock(GymClassRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository, clock);

        when(gymClassRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class, 
            () -> service.getBookingsForClass(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(bookingRepository, never())
            .findByGymClassIdOrderByCreatedAtAscIdAsc(99L);
    }

    @Test 
    void shouldReturnBookingsForClass() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GymClassRepository gymClassRepository = mock(GymClassRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository, clock);

        GymClass gymClass = new GymClass(
            1L, 
            "Yoga", 
            1, 
            Instant.now(clock).plus(1, ChronoUnit.HOURS)
        );

        Member member = new Member(2L, "Maria", "maria@example.com");

        Booking booking = new Booking(
            3L,
            gymClass,
            member,
            BookingStatus.CONFIRMED,
            Instant.now(clock).minus(1, ChronoUnit.HOURS)
        );

        when(gymClassRepository.existsById(1L)).thenReturn(true);

        when(bookingRepository.findByGymClassIdOrderByCreatedAtAscIdAsc(1L))
            .thenReturn(List.of(booking));

        List<BookingResponse> result = service.getBookingsForClass(1L);

        assertEquals(1, result.size());

        BookingResponse response = result.get(0);

        assertEquals(3L, response.id());
        assertEquals(1L, response.classId());
        assertEquals(2L, response.memberId());
        assertEquals(BookingStatus.CONFIRMED, response.status());
        assertEquals(booking.getCreatedAt(), response.createdAt());
    }

    @Test
    void shouldRejectCancellationExactlyTwoHoursBeforeStart() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GymClassRepository gymClassRepository = mock(GymClassRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository, clock);

        GymClass gymClass = new GymClass(
            1L, 
            "Yoga", 
            1, 
            Instant.now(clock).plus(2, ChronoUnit.HOURS)
        );

        Member member = new Member(1L, "Maria", "maria@example.com");

        Booking confirmedBooking = new Booking(
            2L,
            gymClass,
            member,
            BookingStatus.CONFIRMED,
            Instant.now(clock).minus(1, ChronoUnit.HOURS)
        );

        when(gymClassRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(gymClass));

        when(bookingRepository.findByGymClassIdAndMemberId(1L, 1L))
            .thenReturn(Optional.of(confirmedBooking));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class, 
            () -> service.cancelBooking(1L, 1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        assertEquals(
            BookingStatus.CONFIRMED,
            confirmedBooking.getBookingStatus()
        );

        verify(bookingRepository, never())
            .findFirstByGymClassIdAndStatusOrderByCreatedAtAscIdAsc(
                1L, BookingStatus.WAITLISTED
            );
    }

    @Test
    void shouldAllowCancellationJustBeforeDeadline() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GymClassRepository gymClassRepository = mock(GymClassRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository, clock);

        GymClass gymClass = new GymClass(
            1L, 
            "Yoga", 
            1, 
            Instant.now(clock).plus(2, ChronoUnit.HOURS).plusSeconds(1)
        );

        Member member = new Member(1L, "Maria", "maria@example.com");

        Booking confirmedBooking = new Booking(
            2L,
            gymClass,
            member,
            BookingStatus.CONFIRMED,
            Instant.now(clock).minus(1, ChronoUnit.HOURS)
        );

        when(bookingRepository.findByGymClassIdAndMemberId(1L, 1L))
            .thenReturn(Optional.of(confirmedBooking));

        when(gymClassRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(gymClass));

        when(bookingRepository
            .findFirstByGymClassIdAndStatusOrderByCreatedAtAscIdAsc(
                1L, BookingStatus.WAITLISTED))
            .thenReturn(Optional.empty());

        BookingResponse response = service.cancelBooking(1L, 1L);

        assertEquals(BookingStatus.CANCELLED, response.status());

        assertEquals(
            BookingStatus.CANCELLED,
            confirmedBooking.getBookingStatus()
        );
    }

    @Test 
    void shouldReturnEmptyListWhenMemberHasNoBookings() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GymClassRepository gymClassRepository = mock(GymClassRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository, clock);

        when(memberRepository.existsById(7L)).thenReturn(true);

        when(bookingRepository.findByMemberIdOrderByCreatedAtDescIdDesc(7L))
            .thenReturn(List.of());

        List<BookingResponse> result = service.getBookingsForMember(7L);

        assertTrue(result.isEmpty());
    }

    @Test 
    void shouldRejectBookingListForMissingMember() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GymClassRepository gymClassRepository = mock(GymClassRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository, clock);

        when(memberRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class, 
            () -> service.getBookingsForMember(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(bookingRepository, never())
            .findByMemberIdOrderByCreatedAtDescIdDesc(99L);

    }

    @Test 
    void shouldReturnBookingsForMember() {
        BookingRepository bookingRepository = mock(BookingRepository.class);
        GymClassRepository gymClassRepository = mock(GymClassRepository.class);
        MemberRepository memberRepository = mock(MemberRepository.class);

        BookingService service = new BookingService(bookingRepository, gymClassRepository, memberRepository, clock);

        GymClass gymClass = new GymClass(
            1L, 
            "Yoga", 
            1, 
            Instant.now(clock).plus(1, ChronoUnit.HOURS)
        );

        Member member = new Member(7L, "Maria", "maria@example.com");

        Booking booking = new Booking(
            3L,
            gymClass,
            member,
            BookingStatus.CONFIRMED,
            Instant.now(clock).minus(1, ChronoUnit.HOURS)
        );

        when(memberRepository.existsById(7L)).thenReturn(true);

        when(bookingRepository.findByMemberIdOrderByCreatedAtDescIdDesc(7L))
            .thenReturn(List.of(booking));

        List<BookingResponse> result = service.getBookingsForMember(7L);

        assertEquals(1, result.size());

        BookingResponse response = result.get(0);

        assertEquals(3L, response.id());
        assertEquals(1L, response.classId());
        assertEquals(7L, response.memberId());
        assertEquals(BookingStatus.CONFIRMED, response.status());
        assertEquals(booking.getCreatedAt(), response.createdAt());
    }
}