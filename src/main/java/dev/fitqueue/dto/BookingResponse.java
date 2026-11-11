package dev.fitqueue.dto;

import java.time.Instant;

import dev.fitqueue.model.BookingStatus;

public record BookingResponse(
    Long id,
    Long classId,
    Long memberId,
    BookingStatus status,
    Instant createdAt
){
}
