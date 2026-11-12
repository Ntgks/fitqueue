package dev.fitqueue.controller;

import org.springframework.web.bind.annotation.RestController;

import dev.fitqueue.dto.BookingResponse;
import dev.fitqueue.dto.CreateBookingRequest;
import dev.fitqueue.service.BookingService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;



@RestController 
public class BookingController {
    
    private final BookingService bookingService;

    public BookingController(BookingService bookingService){
        this.bookingService = bookingService;
    }

    @PostMapping("/api/classes/{classId}/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
        @PathVariable Long classId,
        @Valid @RequestBody CreateBookingRequest request){
            
            return bookingService.createBooking(classId, request.memberId());
    }

    @DeleteMapping("/api/classes/{classId}/bookings/{memberId}")
    public BookingResponse cancelBooking(
        @PathVariable Long classId,
        @PathVariable  Long memberId){
            return bookingService.cancelBooking(classId, memberId);
    }

    @GetMapping("/api/classes/{classId}/bookings")
    public List<BookingResponse> getBookingsForClass(
        @PathVariable Long classId){
            return bookingService.getBookingsForClass(classId);
        }
    
    
}
