package com.hintro.ridepool.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.hintro.ridepool.entity.RideRequest;
import com.hintro.ridepool.entity.RideStatus;

public interface RideRequestRepository extends MongoRepository<RideRequest, String> {
    
    /**
     * Find all waiting ride requests for a specific airport
     * 
     * @param airportCode airport code
     * @param status ride status
     * @return list of waiting ride requests
     */
    @Query("{ 'airportCode': ?0, 'status': ?1 }")
    List<RideRequest> findByAirportCodeAndStatus(String airportCode, RideStatus status);
    
    /**
     * Find all ride requests that belong to a specific group
     * 
     * @param groupId group ID
     * @return list of ride requests in the group
     */
    @Query("{ 'groupId': ?0 }")
    List<RideRequest> findByGroupId(String groupId);
}

