package com.meetpin.core.model

data class LocationUpdate(
    val groupId: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val bearing: Float? = null,
    val speed: Float? = null,
    val accuracy: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)
