package com.nikhil.project.uber.uberApp.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void signupDto_whenBlankAndInvalidEmail_hasValidationErrors() {
        SignupDto signupDto = new SignupDto("", "not-an-email", "123");

        assertThat(validator.validate(signupDto)).hasSize(3);
    }

    @Test
    void rideRequestDto_whenMissingLocationAndPayment_hasValidationErrors() {
        RideRequestDto rideRequestDto = new RideRequestDto();

        assertThat(validator.validate(rideRequestDto)).hasSize(3);
    }

    @Test
    void ratingDto_whenRatingOutsideRange_hasValidationError() {
        RatingDto ratingDto = new RatingDto();
        ratingDto.setRideId(1L);
        ratingDto.setRating(6);

        assertThat(validator.validate(ratingDto))
                .anyMatch(violation -> violation.getMessage().equals("Rating must be at most 5"));
    }

    @Test
    void rideStartDto_whenOtpNotFourDigits_hasValidationError() {
        RideStartDto rideStartDto = new RideStartDto();
        rideStartDto.setOtp("12");

        assertThat(validator.validate(rideStartDto))
                .anyMatch(violation -> violation.getMessage().equals("OTP must be a 4 digit code"));
    }
}
