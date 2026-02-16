package com.hintro.ridepool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestDTO {

    private String userId;
    private double pickupLat;
    private double pickupLng;
    private String airportCode;
    private int seatsRequired;
    private int luggageCount;
}
