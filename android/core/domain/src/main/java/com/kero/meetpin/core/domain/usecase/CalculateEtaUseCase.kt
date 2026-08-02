package com.kero.meetpin.core.domain.usecase

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

    /**
     * 이동 수단([TravelMode]) 기반의 고도화된 ETA. 출발 알림(`eta-departure-alert`)이 사용한다.
     *
     * 지도 라벨용 단순 ETA([invoke] 속도 오버로드)와 달리 두 가지를 더 반영한다.
     *  - **우회 계수**: 직선거리는 실제 도보/도로 경로보다 짧으므로 [STRAIGHT_LINE_DETOUR_FACTOR]
     *    를 곱해 경로 거리로 보정한다.
     *  - **고정 지연**: 대중교통의 대기·환승처럼 이동 거리와 무관한 시간([TravelMode.fixedOverheadMinutes]).
     *
     * @param distanceMeters 목적지까지의 직선 거리(미터)
     * @param mode 이동 수단
     * @return 예상 소요 시간(분). 이미 도착했으면(거리 0 이하) null
     */
    operator fun invoke(
        distanceMeters: Float,
        mode: TravelMode
    ): Int? {
        if (distanceMeters <= 0f) return null

        val routeMeters = distanceMeters * STRAIGHT_LINE_DETOUR_FACTOR
        val travelMinutes = invoke(distanceMeters = routeMeters, speedKmh = mode.speedKmh)
            ?: return null
        return travelMinutes + mode.fixedOverheadMinutes
    }

    companion object {
        const val DEFAULT_WALKING_SPEED_KMH = 5f

        /**
         * 직선거리 → 실제 경로 거리 보정 계수. 도심 도로망의 우회를 감안한 경험적 근사값이며,
         * 라우팅 API 연동 시 실제 경로 거리로 대체된다.
         */
        const val STRAIGHT_LINE_DETOUR_FACTOR = 1.3f

        private const val METERS_PER_KM = 1000f
        private const val SECONDS_PER_HOUR = 3600f
        private const val SECONDS_PER_MINUTE = 60f
    }
}
