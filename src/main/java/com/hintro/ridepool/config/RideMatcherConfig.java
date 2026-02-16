package com.hintro.ridepool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for ride pooling matching engine.
 * Values can be overridden in application.properties or application.yml
 */
@Configuration
@ConfigurationProperties(prefix = "ridepool.matching")
public class RideMatcherConfig {
    
    /**
     * Matching radius in kilometers (default: 5 KM)
     * Users within this radius can be matched together
     */
    private double matchingRadiusKm = 5.0;
    
    /**
     * Cab capacity in seats (default: 4 seats)
     * Maximum passengers that can be grouped
     */
    private int cabCapacitySeats = 4;
    
    /**
     * Enable or disable matching engine (default: true)
     */
    private boolean enableMatching = true;
    
    // Getters and Setters
    public double getMatchingRadiusKm() {
        return matchingRadiusKm;
    }
    
    public void setMatchingRadiusKm(double matchingRadiusKm) {
        this.matchingRadiusKm = matchingRadiusKm;
    }
    
    public int getCabCapacitySeats() {
        return cabCapacitySeats;
    }
    
    public void setCabCapacitySeats(int cabCapacitySeats) {
        this.cabCapacitySeats = cabCapacitySeats;
    }
    
    public boolean isEnableMatching() {
        return enableMatching;
    }
    
    public void setEnableMatching(boolean enableMatching) {
        this.enableMatching = enableMatching;
    }
}
