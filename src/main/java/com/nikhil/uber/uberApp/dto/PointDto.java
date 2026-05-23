package com.nikhil.uber.uberApp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PointDto {

    @NotNull(message = "Coordinates are required")
    @Size(min = 2, max = 2, message = "Point coordinates must contain longitude and latitude")
    private double[] coordinates;
    private String type = "Point";

    public PointDto(double[] coordinates) {
        this.coordinates = coordinates;
    }
}
