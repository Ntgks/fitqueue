package dev.fitqueue.controller;

import org.springframework.web.bind.annotation.RestController;

import dev.fitqueue.dto.BookingResponse;
import dev.fitqueue.service.BookingService;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import dev.fitqueue.service.MemberService;

import java.security.Principal;
import dev.fitqueue.dto.MemberResponse;



@RestController 
public class BookingController {
    
    private final BookingService bookingService;
    private final MemberService memberService;

    public BookingController(BookingService bookingService, MemberService memberService){
        this.bookingService = bookingService;
        this.memberService = memberService;
    }

    @PostMapping("/api/classes/{classId}/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
        @PathVariable Long classId,
        Principal principal){
            
            MemberResponse member = 
                memberService.getMemberByEmail(principal.getName());

            return bookingService.createBooking(classId, member.id());
    }

    @DeleteMapping("/api/classes/{classId}/bookings/me")
    public BookingResponse cancelBooking(
        @PathVariable Long classId,
        Principal principal){
            MemberResponse member = 
                memberService.getMemberByEmail(principal.getName());

            return bookingService.cancelBooking(classId, member.id());
    }

    @GetMapping("/api/classes/{classId}/bookings")
    public List<BookingResponse> getBookingsForClass(
        @PathVariable Long classId){
            return bookingService.getBookingsForClass(classId);
    }

    @GetMapping("/api/bookings/me")
    public List<BookingResponse> getMyBookings(Principal principal) {
        MemberResponse member = 
            memberService.getMemberByEmail(principal.getName());

        return bookingService.getBookingsForMember(member.id());
    }
    
    
}
