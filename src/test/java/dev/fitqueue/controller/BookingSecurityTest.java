package dev.fitqueue.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import dev.fitqueue.config.SecurityConfig;
import dev.fitqueue.service.BookingService;
import dev.fitqueue.service.MemberService;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import dev.fitqueue.dto.MemberResponse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.Instant;
import dev.fitqueue.dto.BookingResponse;
import dev.fitqueue.model.BookingStatus;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;



@WebMvcTest(BookingController.class)
@Import(SecurityConfig.class)
public class BookingSecurityTest {
    
    @Autowired 
    MockMvc mockMvc;

    @MockitoBean 
    BookingService bookingService;

    @MockitoBean 
    MemberService memberService;

    @Test 
    @WithMockUser(roles = "MEMBER")
    void shouldForbidMemberFromListingClassBookings() throws Exception {
        mockMvc.perform(get("/api/classes/4/bookings"))
            .andExpect(status().isForbidden());

        verifyNoInteractions(bookingService, memberService);
    }

    @Test 
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToListClassBookings() throws Exception {
        when(bookingService.getBookingsForClass(4L))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/classes/4/bookings"))
            .andExpect(status().isOk());

        verify(bookingService).getBookingsForClass(4L);
    }

    @Test 
    @WithMockUser(
        username = "maria@example.com",
        roles = "MEMBER"
    )
    void shouldCreateBookingForAuthenticatedMember() throws Exception {
        when(memberService.getMemberByEmail("maria@example.com"))
            .thenReturn(new MemberResponse(
                7L, "Maria", "maria@example.com"
            ));
        
        
        when(bookingService.createBooking(4L, 7L))
            .thenReturn(new BookingResponse(
                10L,
                4L,
                7L,
                BookingStatus.CONFIRMED,
                Instant.parse("2030-06-15T12:00:00Z")
            ));

        mockMvc.perform(post("/api/classes/4/bookings").with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.memberId").value(7))
            .andExpect(jsonPath("$.status").value("CONFIRMED"));
        
        verify(bookingService).createBooking(4L, 7L);
    }

    @Test 
    @WithMockUser(username = "maria@example.com", roles = "MEMBER")
    void shouldRejectBookingWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/classes/4/bookings"))
            .andExpect(status().isForbidden());

        verifyNoInteractions(bookingService, memberService);
    }

    @Test 
    @WithMockUser(
        username = "maria@example.com",
        roles = "MEMBER"
    )
    void shouldCancelBookingForAuthenticatedMember() throws Exception {
        when(memberService.getMemberByEmail("maria@example.com"))
            .thenReturn(new MemberResponse(
                7L, "Maria", "maria@example.com"
        ));

        when(bookingService.cancelBooking(4L, 7L))
            .thenReturn(new BookingResponse(
                10L,
                4L,
                7L,
                BookingStatus.CANCELLED,
                Instant.parse("2030-06-15T12:00:00Z")
        ));

        mockMvc.perform(delete("/api/classes/4/bookings/me").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.memberId").value(7))
            .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(bookingService).cancelBooking(4L, 7L);
    }

    @Test 
    @WithMockUser(
        username = "maria@example.com",
        roles = "MEMBER"
    )
    void shouldRejectCancellationWithoutCsrfToken() throws Exception {
        mockMvc.perform(delete("/api/classes/4/bookings/me"))
            .andExpect(status().isForbidden());

        verifyNoInteractions(bookingService, memberService);

    }
}
