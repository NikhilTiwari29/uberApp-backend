package com.nikhil.project.uber.uberApp.utils;

import com.nikhil.project.uber.uberApp.dto.PointDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeometryUtilTest {

    @Test
    void createPoint_usesLongitudeLatitudeAndSrid4326() {
        PointDto pointDto = new PointDto(new double[]{77.5946, 12.9716});

        var point = GeometryUtil.createPoint(pointDto);

        assertThat(point.getX()).isEqualTo(77.5946);
        assertThat(point.getY()).isEqualTo(12.9716);
        assertThat(point.getSRID()).isEqualTo(4326);
    }
}
