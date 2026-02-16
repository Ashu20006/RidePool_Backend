package com.hintro.ridepool.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ride_requests")
public class RideRequest {

    @Id
    private String id;

    private String userId;
    private double pickupLat;
    private double pickupLng;
    private String airportCode;
    private int seatsRequired;
    private int luggageCount;
    
    @CreatedDate
    private Instant requestTime;
    
    private RideStatus status;
    
    /**
     * Group ID - links this ride request to other matched ride requests.
     * When users are matched, they share the same groupId
     */
    private String groupId;
    
    /**
     * Cab Assignment Fields
     */
    private String assignedCabId;           // ID of assigned cab
    private String assignedDriverName;      // Driver name
    private Instant cabArrivalTime;         // Estimated arrival time
}
