package com.hintro.ridepool.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hintro.ridepool.entity.Cab;
import com.hintro.ridepool.entity.CabStatus;
import com.hintro.ridepool.repository.CabRepository;

@Service
public class CabService {
    
    private static final Logger logger = LoggerFactory.getLogger(CabService.class);
    private final CabRepository cabRepository;
    
    public CabService(CabRepository cabRepository) {
        this.cabRepository = cabRepository;
    }
    
    /**
     * Create a cab with driver details and location
     * 
     * @param driverName name of the driver
     * @param currentLat current latitude
     * @param currentLng current longitude
     * @param totalSeats total seats in cab
     * @param luggageCapacity luggage capacity
     * @return saved cab
     */
    public Cab createCab(String driverName, double currentLat, double currentLng, 
                        int totalSeats, int luggageCapacity) {
        try {
            Cab cab = Cab.builder()
                    .driverName(driverName)
                    .currentLat(currentLat)
                    .currentLng(currentLng)
                    .totalSeats(totalSeats)
                    .availableSeats(totalSeats)
                    .luggageCapacity(luggageCapacity)
                    .availableLuggage(luggageCapacity)
                    .status(CabStatus.AVAILABLE)
                    .build();
            
            logger.info("Attempting to save cab: Driver: {}, Location: ({}, {})", 
                    driverName, currentLat, currentLng);
            Cab savedCab = cabRepository.save(cab);
            
            logger.info("✓ Cab successfully saved to database with ID: {}", savedCab.getId());
            System.out.println("✓ Cab successfully saved to database with ID: " + savedCab.getId());
            
            // Verify the cab was actually saved
            Cab retrievedCab = cabRepository.findById(savedCab.getId()).orElse(null);
            if (retrievedCab != null) {
                logger.info("✓ Verification: Cab retrieved from database: {}", retrievedCab);
                System.out.println("✓ Verification: Cab retrieved from database!");
            } else {
                logger.warn("⚠ Warning: Cab was not found in database after saving!");
                System.out.println("⚠ Warning: Cab was not found in database after saving!");
            }
            
            return savedCab;
        } catch (Exception e) {
            logger.error("✗ Error saving cab to database", e);
            System.err.println("✗ Error saving cab: " + e.getMessage());
            throw new RuntimeException("Failed to save cab", e);
        }
    }
    
    /**
     * Get cab by ID
     * 
     * @param id cab ID
     * @return cab if found, null otherwise
     */
    public Cab getCabById(String id) {
        return cabRepository.findById(id).orElse(null);
    }
}

