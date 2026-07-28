package com.kero.meetpin.core.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * 위치 수집 클라이언트 인터페이스.
 * FusedLocationProviderClient 기반 구현체를 DI 교체 가능하도록 추상화.
 */
interface LocationClient {

    /**
     * 실시간 위치 업데이트를 Flow로 방출한다.
     * @param intervalMs 위치 업데이트 요청 간격 (밀리초)
     * @param minDistanceM 최소 이동 거리 (미터) - 이 거리 이상 이동해야 업데이트
     */
    fun getLocationUpdates(
        intervalMs: Long = DEFAULT_INTERVAL_MS,
        minDistanceM: Float = DEFAULT_MIN_DISTANCE_M
    ): Flow<Location>

    /**
     * 마지막으로 알려진 위치를 반환한다.
     */
    suspend fun getLastKnownLocation(): Location?

    companion object {
        const val DEFAULT_INTERVAL_MS = 3_000L
        const val DEFAULT_MIN_DISTANCE_M = 5f
        const val MIN_ACCURACY_THRESHOLD = 20f // 정확도 20m 이하만 허용
    }
}
