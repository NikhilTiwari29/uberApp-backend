package com.nikhil.project.uber.uberApp.repositories;

import com.nikhil.project.uber.uberApp.entities.RideRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RideRequestRepository extends JpaRepository<RideRequest, Long> {

    @Modifying
    @Query(value = """
            UPDATE ride_request
            SET ride_request_status = 'CONFIRMED'
            WHERE id = :rideRequestId
              AND ride_request_status = 'PENDING'
            """, nativeQuery = true)
    int markConfirmedIfPending(Long rideRequestId);
}
