package com.hintro.ridepool.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hintro.ridepool.entity.Cab;
import com.hintro.ridepool.service.CabService;

/**
 * Controller for cab operations
 */
@RestController
@RequestMapping("/cabs")
public class CabController {

    private final CabService cabService;

    public CabController(CabService cabService) {
        this.cabService = cabService;
    }

    /**
     * Create a new cab with driver details
     * 
     * Request body:
     * {
     *   "driverName": "Raj Kumar",
     *   "currentLat": 28.5355,
     *   "currentLng": 77.0423,
     *   "totalSeats": 4,
     *   "luggageCapacity": 10
     * }
     */
    @PostMapping
    public ResponseEntity<Cab> createCab(@RequestBody CabRequest request) {
        try {
            System.out.println("✓ Received cab creation request: " + request.getDriverName());
            
            Cab savedCab = cabService.createCab(
                    request.getDriverName(),
                    request.getCurrentLat(),
                    request.getCurrentLng(),
                    request.getTotalSeats(),
                    request.getLuggageCapacity()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCab);
        } catch (Exception e) {
            System.err.println("✗ Error creating cab: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Request DTO for cab creation
     */
    public static class CabRequest {
        private String driverName;
        private double currentLat;
        private double currentLng;
        private int totalSeats;
        private int luggageCapacity;

        // Getters and Setters
        public String getDriverName() {
            return driverName;
        }

        public void setDriverName(String driverName) {
            this.driverName = driverName;
        }

        public double getCurrentLat() {
            return currentLat;
        }

        public void setCurrentLat(double currentLat) {
            this.currentLat = currentLat;
        }

        public double getCurrentLng() {
            return currentLng;
        }

        public void setCurrentLng(double currentLng) {
            this.currentLng = currentLng;
        }

        public int getTotalSeats() {
            return totalSeats;
        }

        public void setTotalSeats(int totalSeats) {
            this.totalSeats = totalSeats;
        }

        public int getLuggageCapacity() {
            return luggageCapacity;
        }

        public void setLuggageCapacity(int luggageCapacity) {
            this.luggageCapacity = luggageCapacity;
        }
    }
}
