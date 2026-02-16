package com.hintro.ridepool.entity;

/**
 * Cab status enum
 */
public enum CabStatus {
    AVAILABLE,  // Available to accept new rides
    RESERVED,   // Reserved for a group (waiting for pickup to start)
    ON_TRIP,    // Currently on trip with passengers
    MAINTENANCE // Under maintenance
}