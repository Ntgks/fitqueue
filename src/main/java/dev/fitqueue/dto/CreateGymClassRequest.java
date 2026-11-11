package dev.fitqueue.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateGymClassRequest(
    @NotBlank String title, 
    @Min(1) int capacity
){
}