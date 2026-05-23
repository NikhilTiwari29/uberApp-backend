package com.nikhil.project.uber.uberApp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OnboardDriverDto {
    @NotBlank(message = "Vehicle id is required")
    private String vehicleId;
}
