package com.hintro.ridepool.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hintro.ridepool.dto.MatchedRideGroup;
import com.hintro.ridepool.dto.RideRequestDTO;
import com.hintro.ridepool.entity.RideRequest;
import com.hintro.ridepool.entity.RideStatus;
import com.hintro.ridepool.matcher.RideMatcher;
import com.hintro.ridepool.repository.RideRequestRepository;

/**
 * Service layer for ride request operations
 * 
 * Responsibilities:
 * 1. Handle ride request creation
 * 2. Integrate with matching engine
 * 3. Integrate with cab assignment engine
 * 4. Manage request updates
 * 
 * Workflow:
 * Request Creation → Matching → Cab Assignment (if group full or min passengers)
 */
@Service
public class RideRequestService {
    
    private static final Logger logger = LoggerFactory.getLogger(RideRequestService.class);
    private final RideRequestRepository rideRequestRepository;
    private final RideMatcher rideMatcher;
    private final CabAssignmentService cabAssignmentService;
    
    public RideRequestService(RideRequestRepository rideRequestRepository,
                            RideMatcher rideMatcher,
                            CabAssignmentService cabAssignmentService) {
        this.rideRequestRepository = rideRequestRepository;
        this.rideMatcher = rideMatcher;
        this.cabAssignmentService = cabAssignmentService;
    }
    
    /**
     * Create a new ride request from passenger
     * Flow:
     * 1. Save request to MongoDB
     * 2. Trigger matching engine
     * 3. Update group statuses if matches found
     * 4. Trigger cab assignment if group has minimum passengers
     * 
     * @param dto RideRequestDTO with passenger details
     * @return saved RideRequest
     */
    public RideRequest createRideRequest(RideRequestDTO dto) {
        try {
            logger.info("========================================");
            logger.info("Creating new ride request for user: {}", dto.getUserId());
            logger.info("========================================");
            
            // Step 1: Create and save the new request
            RideRequest rideRequest = RideRequest.builder()
                    .userId(dto.getUserId())
                    .pickupLat(dto.getPickupLat())
                    .pickupLng(dto.getPickupLng())
                    .airportCode(dto.getAirportCode())
                    .seatsRequired(dto.getSeatsRequired())
                    .luggageCount(dto.getLuggageCount())
                    .requestTime(Instant.now())
                    .status(RideStatus.WAITING)
                    .build();
            
            RideRequest savedRequest = rideRequestRepository.save(rideRequest);
            
            logger.info("✓ Ride request successfully created with ID: {}", savedRequest.getId());
            System.out.println("✓ Ride request successfully created with ID: " + savedRequest.getId());
            
            // Verify the ride request was saved
            RideRequest retrievedRequest = rideRequestRepository.findById(savedRequest.getId()).orElse(null);
            if (retrievedRequest != null) {
                logger.info("✓ Verification: Ride request retrieved from database");
                System.out.println("✓ Verification: Ride request retrieved from database!");
            } else {
                logger.warn("⚠ Warning: Ride request was not found in database after saving!");
                System.out.println("⚠ Warning: Ride request was not found in database after saving!");
            }
            
            // Step 2: Trigger matching engine
            logger.info("------------ INITIATING MATCHING ENGINE ----------");
            MatchedRideGroup matchedGroup = rideMatcher.findAndGroupMatches(savedRequest);
            logger.info("------------ MATCHING ENGINE COMPLETE ----------");
            
            // Step 3: Log matching results
            logMatchingResults(matchedGroup, savedRequest);
            
            // Step 4: Trigger cab assignment if conditions met
            logger.info("------------ INITIATING CAB ASSIGNMENT ----------");
            boolean cabAssigned = cabAssignmentService.attemptCabAssignment(matchedGroup);
            if (cabAssigned) {
                logger.info("------------ CAB ASSIGNMENT SUCCESSFUL ----------");
            } else {
                logger.info("------------ CAB ASSIGNMENT SKIPPED/FAILED ----------");
            }
            
            logger.info("========================================");
            logger.info("Ride request creation workflow completed");
            logger.info("========================================");
            
            return savedRequest;
            
        } catch (Exception e) {
            logger.error("✗ Error creating ride request", e);
            System.err.println("✗ Error creating ride request: " + e.getMessage());
            throw new RuntimeException("Failed to create ride request", e);
        }
    }
    
    /**
     * Get ride request by ID
     * 
     * @param id ride request ID
     * @return RideRequest if found, null otherwise
     */
    public RideRequest getRideRequestById(String id) {
        logger.debug("Fetching ride request with ID: {}", id);
        return rideRequestRepository.findById(id).orElse(null);
    }
    
    /**
     * Log matching results for debugging and monitoring
     */
    private void logMatchingResults(MatchedRideGroup matchedGroup, RideRequest newRequest) {
        logger.info("========================================");
        logger.info("MATCHING RESULTS");
        logger.info("========================================");
        logger.info("Request ID: {}", newRequest.getId());
        logger.info("Airport: {}", matchedGroup.getAirportCode());
        logger.info("Matched passengers: {}", matchedGroup.getPassengers().size());
        logger.info("Total seats: {}", matchedGroup.getTotalSeatsRequired());
        logger.info("Total luggage: {}", matchedGroup.getTotalLuggageCount());
        logger.info("Group status: {}", matchedGroup.getGroupStatus());
        
        if ("FULL".equals(matchedGroup.getGroupStatus())) {
            logger.info("✓ GROUP FULL - CAB ASSIGNMENT READY");
            System.out.println("✓ GROUP FULL - CAB ASSIGNMENT READY");
        } else {
            logger.info("⏳ GROUP PARTIAL - WAITING FOR MORE PASSENGERS");
            System.out.println("⏳ GROUP PARTIAL - WAITING FOR MORE PASSENGERS");
        }
        
        logger.info("Passengers:");
        matchedGroup.getPassengers().forEach(p -> 
            logger.info("  - User: {}, Seats: {}, Luggage: {}, Status: {}", 
                    p.getUserId(), p.getSeatsRequired(), p.getLuggageCount(), p.getStatus())
        );
        logger.info("========================================");
    }
}

