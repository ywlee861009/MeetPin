package com.meetpin.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateLatePenaltyUseCaseTest {

    private val calculateLatePenalty = CalculateLatePenaltyUseCase()

    private val scheduledAt = 1_700_000_000_000L // 고정된 약속 시각
    private val penaltyPerMinute = 1_000

    private fun penaltyAt(
        minutesFromScheduled: Long,
        isArrived: Boolean = false,
        penaltyPerMinute: Int = this.penaltyPerMinute
    ): LatePenalty = calculateLatePenalty(
        scheduledAt = scheduledAt,
        now = scheduledAt + minutesFromScheduled * 60_000L,
        isArrived = isArrived,
        penaltyPerMinute = penaltyPerMinute
    )

    @Test
    fun `약속 시간 전이면 지각이 아니다`() {
        assertEquals(LatePenalty.NONE, penaltyAt(minutesFromScheduled = -10))
    }

    @Test
    fun `정확히 약속 시간이면 지각이 아니다`() {
        assertEquals(LatePenalty.NONE, penaltyAt(minutesFromScheduled = 0))
    }

    @Test
    fun `1분 미만 지각은 0분으로 처리한다`() {
        val result = calculateLatePenalty(
            scheduledAt = scheduledAt,
            now = scheduledAt + 59_000L, // 59초 경과
            isArrived = false,
            penaltyPerMinute = penaltyPerMinute
        )
        assertEquals(LatePenalty.NONE, result)
    }

    @Test
    fun `3분 지각이면 벌칙금은 단가의 3배다`() {
        val result = penaltyAt(minutesFromScheduled = 3)

        assertEquals(3, result.lateMinutes)
        assertEquals(3_000, result.amount)
    }

    @Test
    fun `이미 도착한 참가자는 지각이 누적되지 않는다`() {
        assertEquals(
            LatePenalty.NONE,
            penaltyAt(minutesFromScheduled = 30, isArrived = true)
        )
    }

    @Test
    fun `벌칙금 단가가 0이면 지각 시간만 집계한다`() {
        val result = penaltyAt(minutesFromScheduled = 7, penaltyPerMinute = 0)

        assertEquals(7, result.lateMinutes)
        assertEquals(0, result.amount)
    }

    @Test
    fun `벌칙금 단가가 음수여도 금액은 0 이상이다`() {
        val result = penaltyAt(minutesFromScheduled = 5, penaltyPerMinute = -1_000)

        assertEquals(5, result.lateMinutes)
        assertEquals(0, result.amount)
    }

    @Test
    fun `시간이 지날수록 지각 시간과 벌칙금이 함께 증가한다`() {
        val after1min = penaltyAt(minutesFromScheduled = 1)
        val after10min = penaltyAt(minutesFromScheduled = 10)

        assertEquals(1, after1min.lateMinutes)
        assertEquals(1_000, after1min.amount)
        assertEquals(10, after10min.lateMinutes)
        assertEquals(10_000, after10min.amount)
    }
}
