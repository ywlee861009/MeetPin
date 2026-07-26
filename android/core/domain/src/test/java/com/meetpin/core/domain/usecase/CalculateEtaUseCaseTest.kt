package com.meetpin.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculateEtaUseCaseTest {

    private val calculateEta = CalculateEtaUseCase()

    @Test
    fun `거리가 0이면 null을 반환한다`() {
        assertNull(calculateEta(distanceMeters = 0f))
    }

    @Test
    fun `거리가 음수면 null을 반환한다`() {
        assertNull(calculateEta(distanceMeters = -10f))
    }

    @Test
    fun `속도가 0 이하면 null을 반환한다`() {
        assertNull(calculateEta(distanceMeters = 1000f, speedKmh = 0f))
        assertNull(calculateEta(distanceMeters = 1000f, speedKmh = -5f))
    }

    @Test
    fun `도보 5kmh 기준 1km는 12분이다`() {
        // 1000m / (5km/h = 1.3888m/s) = 720초 = 12분
        assertEquals(12, calculateEta(distanceMeters = 1000f))
    }

    @Test
    fun `도보 5kmh 기준 5km는 60분이다`() {
        assertEquals(60, calculateEta(distanceMeters = 5000f))
    }

    @Test
    fun `1분 미만 거리는 최소 1분으로 보정한다`() {
        // 10m는 약 7초지만 0분으로 표시하지 않는다
        assertEquals(1, calculateEta(distanceMeters = 10f))
    }

    @Test
    fun `소수점 분은 내림 처리한다`() {
        // 100m / 1.3888m/s = 72초 = 1.2분 → 1분
        assertEquals(1, calculateEta(distanceMeters = 100f))
    }

    @Test
    fun `속도를 높이면 소요 시간이 줄어든다`() {
        val walking = calculateEta(distanceMeters = 3000f, speedKmh = 5f)
        val cycling = calculateEta(distanceMeters = 3000f, speedKmh = 15f)

        assertEquals(36, walking)
        assertEquals(12, cycling)
    }
}
