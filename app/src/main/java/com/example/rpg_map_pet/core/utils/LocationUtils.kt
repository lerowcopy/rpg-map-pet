package com.example.rpg_map_pet.core.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Calculates the distance between two geographic points using the Haversine formula.
 * @param lat1 Latitude of the first point
 * @param lng1 Longitude of the first point
 * @param lat2 Latitude of the second point
 * @param lng2 Longitude of the second point
 * @return Distance in meters
 */
fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
    val earthRadius = 6371000 // meters

    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2) * sin(dLng / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return (earthRadius * c).toFloat()
}

/**
 * Checks if a point is within a given radius of a target point.
 * @param userLat User's latitude
 * @param userLng User's longitude
 * @param targetLat Target latitude
 * @param targetLng Target longitude
 * @param radiusMeters Radius in meters
 * @return true if within radius
 */
fun isWithinRadius(
    userLat: Double,
    userLng: Double,
    targetLat: Double,
    targetLng: Double,
    radiusMeters: Float
): Boolean {
    return calculateDistance(userLat, userLng, targetLat, targetLng) <= radiusMeters
}
