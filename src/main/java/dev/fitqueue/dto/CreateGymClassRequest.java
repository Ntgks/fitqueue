package dev.fitqueue.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;

public record CreateGymClassRequest(
    @NotBlank String title, 
    @Min(1) int capacity,
    @NotNull @Future Instant startsAt
){
}