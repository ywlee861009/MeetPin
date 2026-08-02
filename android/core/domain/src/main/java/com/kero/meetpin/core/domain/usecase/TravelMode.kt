package com.kero.meetpin.core.domain.usecase

/**
 * 출발 알림(ETA) 계산에 쓰는 이동 수단.
 *
 * 외부 라우팅(Directions) API 연동 전까지는 직선거리 기반 근사 모델을 쓴다. 각 수단은
 * 평균 순항 속도([speedKmh])와, 이동 자체와 별개로 발생하는 고정 지연([fixedOverheadMinutes])
 * 으로 정의한다. 대중교통은 대기·환승 시간이 실제 소요의 큰 비중이라 고정 지연을 둔다.
 *
 * 직선거리를 실제 경로 거리로 보정하는 우회 계수는 [CalculateEtaUseCase]가 적용한다.
 */
enum class TravelMode(
    val speedKmh: Float,
    val fixedOverheadMinutes: Int
) {
    /** 도보. 평균 4.5km/h, 고정 지연 없음. */
    WALK(speedKmh = 4.5f, fixedOverheadMinutes = 0),

    /** 대중교통. 순항 18km/h + 대기·환승 고정 6분. */
    TRANSIT(speedKmh = 18f, fixedOverheadMinutes = 6)
}
