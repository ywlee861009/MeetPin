package com.kero.meetpin.core.location

import com.kero.meetpin.core.model.PinLocation

/**
 * 약속 장소 도착 감지기.
 *
 * 현재 위치와 약속 핀 위치 간의 거리를 계산하여
 * 반경(기본 50m) 이내 진입 시 도착(Arrived) 이벤트를 발생시킨다.
 *
 * 실제 거리 계산은 [DistanceCalculator] seam에 위임한다. 프로덕션 기본값은
 * Android [android.location.Location] 기반의 [AndroidDistanceCalculator]이며,
 * 단위 테스트에서는 결정론적 Fake를 주입해 판정 로직만 검증한다.
 */
class ArrivalDetector(
    private val arrivalRadiusMeters: Float = DEFAULT_ARRIVAL_RADIUS_METERS,
    private val distanceCalculator: DistanceCalculator = AndroidDistanceCalculator
) {

    /**
     * 현재 위치가 약속 장소 반경 내인지 판단한다.
     *
     * @param currentLat 현재 위도
     * @param currentLng 현재 경도
     * @param pinLocation 약속 장소 위치
     * @return 반경 내 진입 여부
     */
    fun isWithinRadius(
        currentLat: Double,
        currentLng: Double,
        pinLocation: PinLocation
    ): Boolean {
        val distance = calculateDistance(
            currentLat, currentLng,
            pinLocation.latitude, pinLocation.longitude
        )
        return distance <= arrivalRadiusMeters
    }

    /**
     * 현재 위치와 약속 장소 간의 거리(미터)를 계산한다.
     * 실제 계산은 [DistanceCalculator]에 위임한다.
     *
     * @param currentLat 현재 위도
     * @param currentLng 현재 경도
     * @param targetLat 목표 위도
     * @param targetLng 목표 경도
     * @return 거리 (미터)
     */
    fun calculateDistance(
        currentLat: Double,
        currentLng: Double,
        targetLat: Double,
        targetLng: Double
    ): Float = distanceCalculator.distanceMeters(
        currentLat, currentLng,
        targetLat, targetLng
    )

    /**
     * 도착 이벤트를 감지한다.
     * 이전 상태가 미도착이었다가 반경 내로 진입했을 때만 true를 반환한다.
     *
     * @param wasArrived 이전 도착 상태
     * @param currentLat 현재 위도
     * @param currentLng 현재 경도
     * @param pinLocation 약속 장소 위치
     * @return 새로 도착한 경우 true, 이미 도착했거나 아직 반경 밖이면 false
     */
    fun detectArrival(
        wasArrived: Boolean,
        currentLat: Double,
        currentLng: Double,
        pinLocation: PinLocation
    ): Boolean {
        if (wasArrived) return false // 이미 도착한 상태면 재감지 안 함
        return isWithinRadius(currentLat, currentLng, pinLocation)
    }

    companion object {
        const val DEFAULT_ARRIVAL_RADIUS_METERS = 50f
    }
}
