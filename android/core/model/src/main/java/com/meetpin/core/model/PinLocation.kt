package com.meetpin.core.model

data class PinLocation(
    val placeName: String,
    val address: String? = null,
    val latitude: Double,
    val longitude: Double
)
