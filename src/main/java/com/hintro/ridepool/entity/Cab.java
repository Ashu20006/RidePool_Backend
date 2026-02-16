package com.hintro.ridepool.entity;

import java.time.LocalDateTime;

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
@Document(collection = "cabs")
public class Cab {

    @Id
    private String id;

    private String driverName;
    private double currentLat;
    private double currentLng;
    
    private int totalSeats;
    private int availableSeats;
    private int luggageCapacity;
    private int availableLuggage;
    
    private CabStatus status;
    
    /**
     * Group ID - links this cab to the passenger group it's assigned to
     */
    private String assignedGroupId;
    
    @CreatedDate
    private LocalDateTime createdAt;
}