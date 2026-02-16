package com.hintro.ridepool.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility class for calculating distance between two geographic coordinates
 * using Haversine formula.
 * 
 * Haversine formula calculates the great-circle distance between two points
 * on Earth given their latitude and longitude.
 */
@Component
public class DistanceCalculator {
    
    private static final Logger logger = LoggerFactory.getLogger(DistanceCalculator.class);
    
    // Earth's mean radius in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    /**
     * Calculate distance between two geographic coordinates using Haversine formula
     * 
     * @param lat1 Latitude of first point
     * @param lng1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lng2 Longitude of second point
     * @return Distance in kilometers
     */
    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        try {
            // Convert degrees to radians
            double dLat = Math.toRadians(lat2 - lat1);
            double dLng = Math.toRadians(lng2 - lng1);
            
            // Haversine formula
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                      Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                      Math.sin(dLng / 2) * Math.sin(dLng / 2);
            
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = EARTH_RADIUS_KM * c;
            
            logger.debug("Distance calculated between ({}, {}) and ({}, {}): {} km",
                    lat1, lng1, lat2, lng2, String.format("%.2f", distance));
            
            return distance;
        } catch (Exception e) {
            logger.error("Error calculating distance", e);
            throw new RuntimeException("Failed to calculate distance", e);
        }
    }
    
    /**
     * Check if distance between two points is within given radius
     * 
     * @param lat1 Latitude of first point
     * @param lng1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lng2 Longitude of second point
     * @param radiusKm Radius in kilometers
     * @return true if distance is within radius, false otherwise
     */
    public boolean isWithinRadius(double lat1, double lng1, double lat2, double lng2, double radiusKm) {
        double distance = calculateDistance(lat1, lng1, lat2, lng2);
        return distance <= radiusKm;
    }
}
