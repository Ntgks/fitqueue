package dev.fitqueue.service;

import org.springframework.beans.factory.annotation.Autowired;

import dev.fitqueue.repository.GymClassRepository;
import dev.fitqueue.repository.MemberRepository;
import dev.fitqueue.repository.BookingRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import org.testcontainers.junit.jupiter.Container;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import dev.fitqueue.model.GymClass;
import dev.fitqueue.model.Member;

import java.util.concurrent.CountDownLatch;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dev.fitqueue.dto.BookingResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import dev.fitqueue.model.BookingStatus;

@SpringBootTest 
@Testcontainers 
public class BookingConcurrencyTest {
    
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
    void shouldNotOverbookWhenTwoMembersBookConcurrently() throws Exception {
        GymClass gymClass = gymClassRepository.save(
            new GymClass(
                null, 
                "Concurrent Yoga", 
                1, 
                Instant.now().plus(2, ChronoUnit.DAYS)
            )
        );

        Member firstMember = memberRepository.save(
            new Member(null, "Maria", "maria-concurrent@example.com")   
        );

        Member secondMember = memberRepository.save(
            new Member(null, "Vaggelis", "vaggelis-concurrent@example.com")
        );

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var firstResult = executor.submit(() -> {
                ready.countDown();

                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Start signal timed out");
                }

                return bookingService.createBooking(
                    gymClass.getId(), firstMember.getId()
                );
            });

            var secondResult = executor.submit(() -> {
                ready.countDown();

                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Start signal timed out");
                }

                return bookingService.createBooking(
                    gymClass.getId(), 
                    secondMember.getId()
                );
            });

            try {
                assertTrue(
                    ready.await(10, TimeUnit.SECONDS),
                    "Both booking tasks should be ready"
                );
            } finally {
                start.countDown();
            }

            BookingResponse firstResponse = firstResult.get(20, TimeUnit.SECONDS);
            BookingResponse secondResponse = secondResult.get(20, TimeUnit.SECONDS);

            var statuses = List.of(
                firstResponse.status(),
                secondResponse.status()
            );

            assertEquals(
                1L, 
                statuses.stream()
                    .filter(status -> status == BookingStatus.CONFIRMED)
                    .count()
            );

            assertEquals(
                1L, 
                statuses.stream()
                    .filter(status -> status == BookingStatus.WAITLISTED)
                    .count()
            );

            assertEquals(
                1L, 
                bookingRepository.countByGymClassIdAndStatus(
                    gymClass.getId(), BookingStatus.CONFIRMED
                )
            );

            assertEquals(
                1L, 
                bookingRepository.countByGymClassIdAndStatus(
                    gymClass.getId(), BookingStatus.WAITLISTED
                )
            );
        }
    }
}
