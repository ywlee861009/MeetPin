package com.kero.meetpin.core.domain.usecase

import javax.inject.Inject

/**
 * "지금 출발해야 하는가"를 판정한다. 출발 알림(`eta-departure-alert`)의 핵심 결정 로직.
 *
 * 약속 시각까지 남은 시간이, (예상 이동 시간 + 여유 버퍼) 이하로 줄어든 순간이 출발 시점이다.
 * 시간 소스(현재 시각)를 인자로 받는 순수 함수라 [DepartureCheckWorker] 없이 단위 테스트할 수 있다.
 *
 * 약속 시각이 이미 지난 경우(남은 시간 음수)에도 true를 반환한다 — "이미 늦었다"도 출발 대상이며,
 * Worker가 한 번 알린 뒤 스스로 작업을 취소하므로 반복 알림은 발생하지 않는다.
 */
class ShouldDepartNowUseCase @Inject constructor() {

    /**
     * @param nowMillis 현재 시각(epoch millis)
     * @param scheduledAtMillis 약속 시각(epoch millis)
     * @param etaMinutes 목적지까지 예상 이동 시간(분)
     * @param leadBufferMinutes 이동 시간 위에 두는 여유 버퍼(분). 준비·집결 시간을 감안한다.
     * @return 지금 출발해야 하면 true
     */
    operator fun invoke(
        nowMillis: Long,
        scheduledAtMillis: Long,
        etaMinutes: Int,
        leadBufferMinutes: Int = DEFAULT_LEAD_BUFFER_MINUTES
    ): Boolean {
        val remainingMillis = scheduledAtMillis - nowMillis
        val neededMillis = (etaMinutes + leadBufferMinutes).toLong() * MILLIS_PER_MINUTE
        return remainingMillis <= neededMillis
    }

    companion object {
        const val DEFAULT_LEAD_BUFFER_MINUTES = 5

        private const val MILLIS_PER_MINUTE = 60_000L
    }
}
