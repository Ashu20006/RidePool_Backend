package com.hintro.ridepool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for cab assignment engine
 */
@Configuration
@ConfigurationProperties(prefix = "ridepool.assignment")
public class CabAssignmentConfig {
    
    /**
     * Minimum passengers in group before attempting cab assignment
     */
    private int minPassengersForAssignment = 2;
    
    /**
     * Cab search radius in kilometers (default: 10 KM)
     */
    private double cabAssignmentRadiusKm = 10.0;
    
    /**
     * Enable or disable cab assignment (default: true)
     */
    private boolean enableAssignment = true;
    
    /**
     * Estimated time for cab to arrive (in seconds)
     */
    private int estimatedArrivalSeconds = 30;
    
    // Getters and Setters
    public int getMinPassengersForAssignment() {
        return minPassengersForAssignment;
    }
    
    public void setMinPassengersForAssignment(int minPassengersForAssignment) {
        this.minPassengersForAssignment = minPassengersForAssignment;
    }
    
    public double getCabAssignmentRadiusKm() {
        return cabAssignmentRadiusKm;
    }
    
    public void setCabAssignmentRadiusKm(double cabAssignmentRadiusKm) {
        this.cabAssignmentRadiusKm = cabAssignmentRadiusKm;
    }
    
    public boolean isEnableAssignment() {
        return enableAssignment;
    }
    
    public void setEnableAssignment(boolean enableAssignment) {
        this.enableAssignment = enableAssignment;
    }
    
    public int getEstimatedArrivalSeconds() {
        return estimatedArrivalSeconds;
    }
    
    public void setEstimatedArrivalSeconds(int estimatedArrivalSeconds) {
        this.estimatedArrivalSeconds = estimatedArrivalSeconds;
    }
}
