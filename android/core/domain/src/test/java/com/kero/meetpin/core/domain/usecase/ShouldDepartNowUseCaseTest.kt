package com.kero.meetpin.core.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [ShouldDepartNowUseCase] 판정 로직 단위 테스트.
 *
 * 현재 시각을 인자로 받으므로 결정론적이다. 기준 시각 [NOW]를 고정해 두고
 * 약속 시각을 상대적으로 배치해 각 경계를 검증한다.
 */
class ShouldDepartNowUseCaseTest {

    private val shouldDepartNow = ShouldDepartNowUseCase()

    private companion object {
        const val NOW = 1_000_000_000_000L
        const val MINUTE = 60_000L
    }

    @Test
    fun `약속까지 남은 시간이 이동+버퍼보다 많으면 아직 출발 아니다`() {
        // eta 20 + 버퍼 5 = 25분 필요한데, 약속은 40분 뒤 → 아직 여유
        val scheduledAt = NOW + 40 * MINUTE
        assertFalse(
            shouldDepartNow(
                nowMillis = NOW,
                scheduledAtMillis = scheduledAt,
                etaMinutes = 20,
                leadBufferMinutes = 5
            )
        )
    }

    @Test
    fun `남은 시간이 이동+버퍼 이하로 줄면 출발 시점이다`() {
        // eta 20 + 버퍼 5 = 25분 필요, 약속은 딱 25분 뒤 → 경계 포함, 출발
        val scheduledAt = NOW + 25 * MINUTE
        assertTrue(
            shouldDepartNow(
                nowMillis = NOW,
                scheduledAtMillis = scheduledAt,
                etaMinutes = 20,
                leadBufferMinutes = 5
            )
        )
    }

    @Test
    fun `경계 바로 위(1분 더 남음)면 아직 출발 아니다`() {
        val scheduledAt = NOW + 26 * MINUTE
        assertFalse(
            shouldDepartNow(
                nowMillis = NOW,
                scheduledAtMillis = scheduledAt,
                etaMinutes = 20,
                leadBufferMinutes = 5
            )
        )
    }

    @Test
    fun `약속 시각이 이미 지났으면 출발로 본다`() {
        val scheduledAt = NOW - 10 * MINUTE
        assertTrue(
            shouldDepartNow(
                nowMillis = NOW,
                scheduledAtMillis = scheduledAt,
                etaMinutes = 5,
                leadBufferMinutes = 5
            )
        )
    }

    @Test
    fun `기본 버퍼는 5분이다`() {
        // eta 10 + 기본 버퍼 5 = 15분. 약속 15분 뒤 → 출발, 16분 뒤 → 아직.
        assertTrue(
            shouldDepartNow(NOW, NOW + 15 * MINUTE, etaMinutes = 10)
        )
        assertFalse(
            shouldDepartNow(NOW, NOW + 16 * MINUTE, etaMinutes = 10)
        )
    }
}
