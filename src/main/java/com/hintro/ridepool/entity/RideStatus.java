package com.hintro.ridepool.entity;

/**
 * Enum representing the status of a ride request
 */
public enum RideStatus {
    WAITING,    // Initial state - waiting for match
    MATCHED,    // Matched with other users but group not full
    ASSIGNED,   // Group full - cab assigned
    COMPLETED,  // Ride completed
    CANCELLED   // Ride cancelled
}

