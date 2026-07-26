package com.meetpin.core.domain.usecase

import javax.inject.Inject

/**
 * 약속 장소까지 남은 거리로 예상 도착 소요 시간(ETA)을 계산한다.
 *
 * 경로 탐색 API 연동 전까지는 직선 거리와 평균 도보 속도를 기준으로 근사한다.
 * (실제 경로 기반 ETA는 `eta-departure-alert` 티켓에서 다룬다.)
 */
class CalculateEtaUseCase @Inject constructor() {

    /**
     * @param distanceMeters 목적지까지의 거리(미터)
     * @param speedKmh 이동 평균 속도(km/h). 기본값은 도보 5km/h
     * @return 예상 소요 시간(분). 이미 도착했거나(거리 0 이하) 속도가 유효하지 않으면 null
     */
    operator fun invoke(
        distanceMeters: Float,
        speedKmh: Float = DEFAULT_WALKING_SPEED_KMH
    ): Int? {
        if (distanceMeters <= 0f || speedKmh <= 0f) return null

        val speedMetersPerSecond = speedKmh * METERS_PER_KM / SECONDS_PER_HOUR
        val minutes = distanceMeters / speedMetersPerSecond / SECONDS_PER_MINUTE

        // 1분 미만이어도 "곧 도착"을 0분으로 표시하지 않기 위해 최소 1분으로 보정한다.
        return minutes.toInt().coerceAtLeast(1)
    }

    companion object {
        const val DEFAULT_WALKING_SPEED_KMH = 5f

        private const val METERS_PER_KM = 1000f
        private const val SECONDS_PER_HOUR = 3600f
        private const val SECONDS_PER_MINUTE = 60f
    }
}
