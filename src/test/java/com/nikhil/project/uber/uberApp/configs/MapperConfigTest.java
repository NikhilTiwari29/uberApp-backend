package com.nikhil.project.uber.uberApp.configs;

import com.nikhil.project.uber.uberApp.dto.PointDto;
import com.nikhil.project.uber.uberApp.dto.RideDto;
import com.nikhil.project.uber.uberApp.entities.Ride;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import static org.assertj.core.api.Assertions.assertThat;

class MapperConfigTest {

    private final MapperConfig mapperConfig = new MapperConfig();

    @Test
    void modelMapper_mapsPointDtoAndPointBothDirections() {
        var mapper = mapperConfig.modelMapper();

        Point point = mapper.map(new PointDto(new double[]{77.59, 12.97}), Point.class);
        PointDto pointDto = mapper.map(point, PointDto.class);

        assertThat(point.getSRID()).isEqualTo(4326);
        assertThat(pointDto.getCoordinates()).containsExactly(77.59, 12.97);
    }

    @Test
    void modelMapper_handlesNullPoints() {
        var mapper = mapperConfig.modelMapper();
        Ride ride = new Ride();

        RideDto rideDto = mapper.map(ride, RideDto.class);

        assertThat(rideDto.getPickupLocation()).isNull();
        assertThat(rideDto.getDropOffLocation()).isNull();
    }
}
