package com.hintro.ridepool.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.hintro.ridepool.entity.Cab;
import com.hintro.ridepool.entity.CabStatus;

public interface CabRepository extends MongoRepository<Cab, String> {
    
    /**
     * Find all available cabs
     * 
     * @param status cab status
     * @return list of cabs with the given status
     */
    @Query("{ 'status': ?0 }")
    List<Cab> findByStatus(CabStatus status);
}

