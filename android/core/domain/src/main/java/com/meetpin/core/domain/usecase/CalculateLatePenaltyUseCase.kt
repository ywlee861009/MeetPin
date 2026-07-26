package com.meetpin.core.domain.usecase

import javax.inject.Inject

/**
 * 지각 시간과 누적 벌칙금.
 */
data class LatePenalty(
    val lateMinutes: Int,
    val amount: Int
) {
    companion object {
        val NONE = LatePenalty(lateMinutes = 0, amount = 0)
    }
}

/**
 * 약속 시간을 기준으로 참가자의 지각 시간과 벌칙금을 계산한다.
 *
 * 현재 시각을 내부에서 조회하지 않고 파라미터로 받는다 — 테스트에서 시간을 고정하기 위함이다.
 */
class CalculateLatePenaltyUseCase @Inject constructor() {

    /**
     * @param scheduledAt 약속 시각 (epoch millis)
     * @param now 현재 시각 (epoch millis)
     * @param isArrived 이미 도착했는지 여부
     * @param penaltyPerMinute 지각 1분당 벌칙금
     * @return 지각 분과 벌칙금. 도착했거나 약속 시간 전이면 [LatePenalty.NONE]
     */
    operator fun invoke(
        scheduledAt: Long,
        now: Long,
        isArrived: Boolean,
        penaltyPerMinute: Int
    ): LatePenalty {
        // 도착한 참가자는 더 이상 지각이 누적되지 않는다.
        if (isArrived) return LatePenalty.NONE
        if (now <= scheduledAt) return LatePenalty.NONE

        // 1분 미만 지각은 0분으로 처리한다 (내림).
        val lateMinutes = ((now - scheduledAt) / MILLIS_PER_MINUTE).toInt()
        if (lateMinutes <= 0) return LatePenalty.NONE

        return LatePenalty(
            lateMinutes = lateMinutes,
            amount = lateMinutes * penaltyPerMinute.coerceAtLeast(0)
        )
    }

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
    }
}
