package com.hintro.ridepool.dto;

import java.util.List;

import com.hintro.ridepool.entity.RideRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a group of matched ride requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchedRideGroup {
    
    /**
     * List of matched ride requests
     */
    private List<RideRequest> passengers;
    
    /**
     * Total seats required by the group
     */
    private int totalSeatsRequired;
    
    /**
     * Total luggage count for the group
     */
    private int totalLuggageCount;
    
    /**
     * Airport code for the group
     */
    private String airportCode;
    
    /**
     * Group status: PARTIAL (waiting for more), FULL (ready for cab assignment)
     */
    private String groupStatus; // PARTIAL or FULL
}
