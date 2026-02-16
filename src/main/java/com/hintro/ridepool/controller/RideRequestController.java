package com.hintro.ridepool.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hintro.ridepool.dto.RideRequestDTO;
import com.hintro.ridepool.entity.RideRequest;
import com.hintro.ridepool.service.RideRequestService;

@RestController
@RequestMapping("/rides")
public class RideRequestController {
    
    private static final Logger logger = LoggerFactory.getLogger(RideRequestController.class);
    private final RideRequestService rideRequestService;
    
    public RideRequestController(RideRequestService rideRequestService) {
        this.rideRequestService = rideRequestService;
    }
    
    /**
     * Create a new ride request
     * 
     * @param dto RideRequestDTO with passenger details
     * @return ResponseEntity with saved RideRequest
     */
    @PostMapping("/request")
    public ResponseEntity<RideRequest> createRideRequest(@RequestBody RideRequestDTO dto) {
        try {
            logger.info("Received ride request from user: {}", dto.getUserId());
            
            // Validate input
            if (dto.getUserId() == null || dto.getUserId().isEmpty()) {
                logger.warn("Invalid ride request: userId is empty");
                return ResponseEntity.badRequest().build();
            }
            
            if (dto.getAirportCode() == null || dto.getAirportCode().isEmpty()) {
                logger.warn("Invalid ride request: airportCode is empty");
                return ResponseEntity.badRequest().build();
            }
            
            if (dto.getSeatsRequired() <= 0) {
                logger.warn("Invalid ride request: seatsRequired must be greater than 0");
                return ResponseEntity.badRequest().build();
            }
            
            RideRequest savedRideRequest = rideRequestService.createRideRequest(dto);
            
            logger.info("Ride request created successfully with ID: {}", savedRideRequest.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRideRequest);
            
        } catch (Exception e) {
            logger.error("✗ Error creating ride request", e);
            System.err.println("✗ Error creating ride request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
