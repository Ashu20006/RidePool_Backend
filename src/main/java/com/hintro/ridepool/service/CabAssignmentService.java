package com.hintro.ridepool.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hintro.ridepool.config.CabAssignmentConfig;
import com.hintro.ridepool.dto.MatchedRideGroup;
import com.hintro.ridepool.entity.Cab;
import com.hintro.ridepool.entity.CabStatus;
import com.hintro.ridepool.entity.RideRequest;
import com.hintro.ridepool.entity.RideStatus;
import com.hintro.ridepool.repository.CabRepository;
import com.hintro.ridepool.repository.RideRequestRepository;
import com.hintro.ridepool.util.DistanceCalculator;

/**
 * Cab Assignment Service
 * 
 * Responsible for:
 * 1. Finding available cabs near passenger group
 * 2. Reserving the nearest cab
 * 3. Assigning cab to all passengers
 * 4. Updating group status to CAB_ASSIGNED
 */
@Service
public class CabAssignmentService {
    
    private static final Logger logger = LoggerFactory.getLogger(CabAssignmentService.class);
    
    private final CabRepository cabRepository;
    private final RideRequestRepository rideRequestRepository;
    private final DistanceCalculator distanceCalculator;
    private final CabAssignmentConfig assignmentConfig;
    
    public CabAssignmentService(CabRepository cabRepository,
                               RideRequestRepository rideRequestRepository,
                               DistanceCalculator distanceCalculator,
                               CabAssignmentConfig assignmentConfig) {
        this.cabRepository = cabRepository;
        this.rideRequestRepository = rideRequestRepository;
        this.distanceCalculator = distanceCalculator;
        this.assignmentConfig = assignmentConfig;
    }
    
    /**
     * Attempt to assign a cab to the matched group
     * 
     * @param matchedGroup the matched ride group
     * @return true if cab assigned successfully, false otherwise
     */
    public boolean attemptCabAssignment(MatchedRideGroup matchedGroup) {
        logger.info("========================================");
        logger.info("CAB ASSIGNMENT ENGINE STARTED");
        logger.info("========================================");
        logger.info("Group: {}, Airport: {}, Passengers: {}", 
                matchedGroup.getPassengers().get(0).getGroupId(),
                matchedGroup.getAirportCode(),
                matchedGroup.getPassengers().size());
        
        try {
            // Check if assignment is enabled
            if (!assignmentConfig.isEnableAssignment()) {
                logger.warn("Cab assignment is disabled in configuration");
                return false;
            }
            
            // Check minimum passengers requirement
            if (matchedGroup.getPassengers().size() < assignmentConfig.getMinPassengersForAssignment()) {
                logger.info("⏳ Group has {} passengers, minimum required: {}", 
                        matchedGroup.getPassengers().size(),
                        assignmentConfig.getMinPassengersForAssignment());
                return false;
            }
            
            // Step 1: Calculate average pickup location
            double[] avgLocation = calculateAverageLocation(matchedGroup.getPassengers());
            logger.info("Group center location: Lat: {}, Lng: {}", 
                    String.format("%.4f", avgLocation[0]), 
                    String.format("%.4f", avgLocation[1]));
            
            // Step 2: Find available cabs
            List<Cab> availableCabs = findAvailableCabs();
            logger.info("Found {} available cabs", availableCabs.size());
            
            if (availableCabs.isEmpty()) {
                logger.warn("⚠ No available cabs found for assignment");
                return false;
            }
            
            // Step 3: Find nearest cab within assignment radius
            Cab nearestCab = findNearestCab(avgLocation[0], avgLocation[1], availableCabs);
            
            if (nearestCab == null) {
                logger.warn("⚠ No cab found within {} KM radius", 
                        assignmentConfig.getCabAssignmentRadiusKm());
                return false;
            }
            
            logger.info("✓ Nearest cab found: ID: {}, Driver: {}", 
                    nearestCab.getId(), nearestCab.getDriverName());
            
            // Step 4: Reserve the cab (atomic operation)
            boolean reserved = reserveCab(nearestCab, matchedGroup);
            if (!reserved) {
                logger.warn("⚠ Failed to reserve cab (race condition or unavailable)");
                return false;
            }
            
            // Step 5: Assign cab to all passengers
            boolean assigned = assignCabToPassengers(matchedGroup, nearestCab);
            if (!assigned) {
                logger.warn("⚠ Failed to assign cab to passengers");
                // Try to unreserve cab
                unreserveCab(nearestCab);
                return false;
            }
            
            logger.info("========================================");
            logger.info("✓ CAB ASSIGNMENT SUCCESSFUL");
            logger.info("========================================");
            return true;
            
        } catch (Exception e) {
            logger.error("✗ Error during cab assignment", e);
            return false;
        }
    }
    
    /**
     * Calculate average pickup location for the group
     */
    private double[] calculateAverageLocation(List<RideRequest> passengers) {
        logger.debug("Calculating average pickup location for {} passengers", passengers.size());
        
        double sumLat = 0;
        double sumLng = 0;
        
        for (RideRequest passenger : passengers) {
            sumLat += passenger.getPickupLat();
            sumLng += passenger.getPickupLng();
        }
        
        double avgLat = sumLat / passengers.size();
        double avgLng = sumLng / passengers.size();
        
        logger.debug("Average location calculated");
        return new double[]{avgLat, avgLng};
    }
    
    /**
     * Find all available cabs
     */
    private List<Cab> findAvailableCabs() {
        logger.debug("Querying for available cabs");
        return cabRepository.findByStatus(CabStatus.AVAILABLE);
    }
    
    /**
     * Find nearest cab within assignment radius
     */
    private Cab findNearestCab(double pickupLat, double pickupLng, List<Cab> availableCabs) {
        logger.debug("Finding nearest cab from {} available cabs", availableCabs.size());
        
        double assignmentRadius = assignmentConfig.getCabAssignmentRadiusKm();
        Cab nearestCab = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (Cab cab : availableCabs) {
            // Check if within radius
            boolean withinRadius = distanceCalculator.isWithinRadius(
                    pickupLat, pickupLng,
                    cab.getCurrentLat(), cab.getCurrentLng(),
                    assignmentRadius
            );
            
            if (!withinRadius) {
                logger.debug("Cab {} is outside {} KM radius", cab.getId(), assignmentRadius);
                continue;
            }
            
            // Calculate distance
            double distance = distanceCalculator.calculateDistance(
                    pickupLat, pickupLng,
                    cab.getCurrentLat(), cab.getCurrentLng()
            );
            
            // Update nearest
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestCab = cab;
                logger.debug("Found closer cab {} at distance: {} KM", 
                        cab.getId(), String.format("%.2f", distance));
            }
        }
        
        if (nearestCab != null) {
            logger.info("Nearest cab: ID: {}, Distance: {} KM", 
                    nearestCab.getId(), String.format("%.2f", nearestDistance));
        }
        
        return nearestCab;
    }
    
    /**
     * Reserve cab by changing status to RESERVED
     */
    private boolean reserveCab(Cab cab, MatchedRideGroup matchedGroup) {
        logger.info("Attempting to reserve cab: {}", cab.getId());
        
        try {
            // Update cab status
            cab.setStatus(CabStatus.RESERVED);
            cab.setAssignedGroupId(matchedGroup.getPassengers().get(0).getGroupId());
            
            Cab savedCab = cabRepository.save(cab);
            
            logger.info("✓ Cab reserved successfully: ID: {}", savedCab.getId());
            return true;
            
        } catch (Exception e) {
            logger.error("✗ Error reserving cab", e);
            return false;
        }
    }
    
    /**
     * Unreserve cab if assignment fails
     */
    private void unreserveCab(Cab cab) {
        logger.warn("Unreserving cab: {}", cab.getId());
        
        try {
            cab.setStatus(CabStatus.AVAILABLE);
            cab.setAssignedGroupId(null);
            cabRepository.save(cab);
            logger.info("✓ Cab unreserved");
        } catch (Exception e) {
            logger.error("✗ Error unreserving cab", e);
        }
    }
    
    /**
     * Assign cab to all passengers in the group
     */
    private boolean assignCabToPassengers(MatchedRideGroup matchedGroup, Cab cab) {
        logger.info("Assigning cab to {} passengers", matchedGroup.getPassengers().size());
        
        try {
            // Calculate estimated arrival time (30 seconds from now as example)
            Instant cabArrivalTime = Instant.now().plusSeconds(30);
            
            // Update all passengers
            for (RideRequest passenger : matchedGroup.getPassengers()) {
                passenger.setAssignedCabId(cab.getId());
                passenger.setAssignedDriverName(cab.getDriverName());
                passenger.setCabArrivalTime(cabArrivalTime);
                passenger.setStatus(RideStatus.ASSIGNED);
                
                rideRequestRepository.save(passenger);
                logger.debug("Updated passenger {} with cab assignment", passenger.getUserId());
            }
            
            logger.info("✓ Successfully assigned cab to all {} passengers", 
                    matchedGroup.getPassengers().size());
            System.out.println("✓ CAB ASSIGNED - Driver arriving in 30 seconds!");
            
            return true;
            
        } catch (Exception e) {
            logger.error("✗ Error assigning cab to passengers", e);
            return false;
        }
    }
}
