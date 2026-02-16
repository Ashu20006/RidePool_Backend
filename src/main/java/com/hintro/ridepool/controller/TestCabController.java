package com.hintro.ridepool.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hintro.ridepool.entity.Cab;
import com.hintro.ridepool.service.CabService;

@RestController
@RequestMapping("/test")
public class TestCabController {

    private final CabService cabService;

    public TestCabController(CabService cabService) {
        this.cabService = cabService;
    }

    /**
     * Create a test cab with default location near Delhi Airport
     */
    @PostMapping("/cab")
    public ResponseEntity<Cab> createDummyCab() {
        try {
            // Create a test cab near Delhi Airport
            // Latitude: 28.5941, Longitude: 77.2282 (Delhi IGI Airport)
            Cab savedCab = cabService.createCab("Rajesh Kumar", 28.5941, 77.2282, 4, 2);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCab);
        } catch (Exception e) {
            System.err.println("✗ Error saving cab: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

