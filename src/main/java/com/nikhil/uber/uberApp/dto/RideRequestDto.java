package com.nikhil.uber.uberApp.dto;

import com.nikhil.project.uber.uberApp.entities.enums.PaymentMethod;
import com.nikhil.project.uber.uberApp.entities.enums.RideRequestStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestDto {

    private Long id;

    @Valid
    @NotNull(message = "Pickup location is required")
    private PointDto pickupLocation;

    @Valid
    @NotNull(message = "Drop off location is required")
    private PointDto dropOffLocation;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private LocalDateTime requestedTime;

    private RiderDto rider;
    private Double fare;

    private RideRequestStatus rideRequestStatus;
}
