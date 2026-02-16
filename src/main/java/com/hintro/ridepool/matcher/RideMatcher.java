package com.hintro.ridepool.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hintro.ridepool.config.RideMatcherConfig;
import com.hintro.ridepool.dto.MatchedRideGroup;
import com.hintro.ridepool.entity.RideRequest;
import com.hintro.ridepool.entity.RideStatus;
import com.hintro.ridepool.repository.RideRequestRepository;
import com.hintro.ridepool.util.DistanceCalculator;

/**
 * Core matching engine for ride pooling
 * Responsible for matching new ride requests with waiting users
 * 
 * Matching Algorithm:
 * 1. Find all WAITING requests for same airport
 * 2. Filter by distance (within matching radius)
 * 3. Group users based on capacity constraints
 * 4. Update status to MATCHED or ASSIGNED
 * 5. Assign groupId to link matched users
 */
@Component
public class RideMatcher {
    
    private static final Logger logger = LoggerFactory.getLogger(RideMatcher.class);
    
    private final RideRequestRepository rideRequestRepository;
    private final DistanceCalculator distanceCalculator;
    private final RideMatcherConfig matcherConfig;
    
    public RideMatcher(RideRequestRepository rideRequestRepository,
                      DistanceCalculator distanceCalculator,
                      RideMatcherConfig matcherConfig) {
        this.rideRequestRepository = rideRequestRepository;
        this.distanceCalculator = distanceCalculator;
        this.matcherConfig = matcherConfig;
    }
    
    /**
     * Find matches for a new ride request and group compatible users
     * 
     * @param newRequest the new ride request to match
     * @return MatchedRideGroup containing matched users or empty group if no match
     */
    public MatchedRideGroup findAndGroupMatches(RideRequest newRequest) {
        logger.info("=== MATCHING ENGINE STARTED for Request ID: {} ===", newRequest.getId());
        logger.debug("New request - User: {}, Airport: {}, Seats: {}, Position: ({}, {})",
                newRequest.getUserId(), newRequest.getAirportCode(), 
                newRequest.getSeatsRequired(), newRequest.getPickupLat(), newRequest.getPickupLng());
        
        // Check if matching is enabled
        if (!matcherConfig.isEnableMatching()) {
            logger.warn("Matching engine is disabled in configuration");
            return MatchedRideGroup.builder()
                    .passengers(List.of(newRequest))
                    .airportCode(newRequest.getAirportCode())
                    .build();
        }
        
        try {
            // Step 1: Find all waiting requests for the same airport (excluding current request)
            List<RideRequest> waitingRequests = findWaitingRequestsForAirport(newRequest);
            logger.info("Found {} waiting requests for airport {}", 
                    waitingRequests.size(), newRequest.getAirportCode());
            
            if (waitingRequests.isEmpty()) {
                logger.warn("No waiting requests found for airport: {}", newRequest.getAirportCode());
                return createSinglePassengerGroup(newRequest);
            }
            
            // Step 2: Filter by distance and capacity
            List<RideRequest> compatibleRequests = filterCompatibleRequests(newRequest, waitingRequests);
            logger.info("Found {} compatible requests within {} KM radius", 
                    compatibleRequests.size(), matcherConfig.getMatchingRadiusKm());
            
            if (compatibleRequests.isEmpty()) {
                logger.warn("No compatible requests found within matching radius");
                return createSinglePassengerGroup(newRequest);
            }
            
            // Step 3: Group compatible users
            MatchedRideGroup matchedGroup = groupUsers(newRequest, compatibleRequests);
            logger.info("Matched group created with {} passengers, Total seats: {}", 
                    matchedGroup.getPassengers().size(), matchedGroup.getTotalSeatsRequired());
            
            // Step 4: Update statuses and assign groupId
            updateGroupStatuses(matchedGroup);
            
            logger.info("=== MATCHING ENGINE COMPLETED - Group Status: {} ===", 
                    matchedGroup.getGroupStatus());
            
            return matchedGroup;
            
        } catch (Exception e) {
            logger.error("Error during matching process", e);
            return createSinglePassengerGroup(newRequest);
        }
    }
    
    /**
     * Find all WAITING requests for the same airport (excluding current request)
     */
    private List<RideRequest> findWaitingRequestsForAirport(RideRequest newRequest) {
        logger.debug("Querying database for waiting requests in airport: {}", newRequest.getAirportCode());
        
        List<RideRequest> requests = rideRequestRepository
                .findByAirportCodeAndStatus(newRequest.getAirportCode(), RideStatus.WAITING);
        
        // Remove the new request itself from the list
        return requests.stream()
                .filter(req -> !req.getId().equals(newRequest.getId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Filter requests that are within matching radius and seat capacity constraints
     */
    private List<RideRequest> filterCompatibleRequests(RideRequest newRequest, 
                                                       List<RideRequest> candidates) {
        logger.debug("Filtering candidates by distance and capacity");
        
        int maxSeats = matcherConfig.getCabCapacitySeats();
        double matchingRadius = matcherConfig.getMatchingRadiusKm();
        
        return candidates.stream()
                .filter(candidate -> {
                    // Check distance
                    boolean withinRadius = distanceCalculator.isWithinRadius(
                            newRequest.getPickupLat(), newRequest.getPickupLng(),
                            candidate.getPickupLat(), candidate.getPickupLng(),
                            matchingRadius
                    );
                    
                    if (!withinRadius) {
                        logger.debug("Candidate {} - Outside matching radius", candidate.getId());
                        return false;
                    }
                    
                    // Check seat capacity
                    int totalSeats = newRequest.getSeatsRequired() + candidate.getSeatsRequired();
                    if (totalSeats > maxSeats) {
                        logger.debug("Candidate {} - Exceeds seat capacity ({}/{})", 
                                candidate.getId(), totalSeats, maxSeats);
                        return false;
                    }
                    
                    logger.debug("Candidate {} is compatible", candidate.getId());
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Group users with compatibility constraints
     * Takes up to (cabCapacity - newRequest.seatsRequired) compatible requests
     */
    private MatchedRideGroup groupUsers(RideRequest newRequest, List<RideRequest> compatibleRequests) {
        logger.info("Grouping users - starting with new request");
        
        List<RideRequest> group = new ArrayList<>();
        group.add(newRequest);
        
        int totalSeats = newRequest.getSeatsRequired();
        int maxSeats = matcherConfig.getCabCapacitySeats();
        int seatsAvailable = maxSeats - totalSeats;
        int totalLuggage = newRequest.getLuggageCount();
        
        // Add compatible requests to group until capacity is reached
        for (RideRequest candidate : compatibleRequests) {
            if (candidate.getSeatsRequired() <= seatsAvailable) {
                group.add(candidate);
                totalSeats += candidate.getSeatsRequired();
                seatsAvailable -= candidate.getSeatsRequired();
                totalLuggage += candidate.getLuggageCount();
                logger.debug("Added user {} to group (Seats used: {}/{})", 
                        candidate.getUserId(), totalSeats, maxSeats);
            } else {
                logger.debug("Cannot add user {} - not enough seats remaining", candidate.getUserId());
            }
        }
        
        // Determine group status
        String groupStatus = (totalSeats == maxSeats) ? "FULL" : "PARTIAL";
        
        MatchedRideGroup matchedGroup = MatchedRideGroup.builder()
                .passengers(group)
                .totalSeatsRequired(totalSeats)
                .totalLuggageCount(totalLuggage)
                .airportCode(newRequest.getAirportCode())
                .groupStatus(groupStatus)
                .build();
        
        logger.info("Group created: {} passengers, {} seats used, Status: {}", 
                group.size(), totalSeats, groupStatus);
        
        return matchedGroup;
    }
    
    /**
     * Update statuses of all users in the group and assign groupId
     */
    private void updateGroupStatuses(MatchedRideGroup matchedGroup) {
        logger.info("Updating statuses for {} passengers in group", matchedGroup.getPassengers().size());
        
        // Generate unique groupId
        String groupId = UUID.randomUUID().toString();
        
        // Determine status based on group status
        RideStatus newStatus = "FULL".equals(matchedGroup.getGroupStatus()) ? 
                RideStatus.ASSIGNED : RideStatus.MATCHED;
        
        logger.info("Assigning groupId: {}, Status: {}", groupId, newStatus);
        
        // Update all passengers
        for (RideRequest passenger : matchedGroup.getPassengers()) {
            passenger.setGroupId(groupId);
            passenger.setStatus(newStatus);
            rideRequestRepository.save(passenger);
            logger.debug("Updated passenger {}: groupId={}, status={}", 
                    passenger.getUserId(), groupId, newStatus);
        }
        
        logger.info("✓ Successfully updated {} passengers' statuses", matchedGroup.getPassengers().size());
    }
    
    /**
     * Create a single passenger group when no matches found
     */
    private MatchedRideGroup createSinglePassengerGroup(RideRequest request) {
        logger.info("No matches found for request {}. Creating single passenger group", request.getId());
        
        return MatchedRideGroup.builder()
                .passengers(List.of(request))
                .totalSeatsRequired(request.getSeatsRequired())
                .totalLuggageCount(request.getLuggageCount())
                .airportCode(request.getAirportCode())
                .groupStatus("PARTIAL")
                .build();
    }
}
