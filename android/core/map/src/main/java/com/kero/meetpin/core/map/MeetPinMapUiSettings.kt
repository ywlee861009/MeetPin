package com.kero.meetpin.core.map

/**
 * 지도 UI 컨트롤 설정 (벤더 중립).
 */
data class MeetPinMapUiSettings(
    val zoomControlsEnabled: Boolean = true,
    val myLocationButtonEnabled: Boolean = false,
    val compassEnabled: Boolean = true,
)
