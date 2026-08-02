package com.kero.meetpin.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    // --- TravelMode 오버로드 (고도화된 ETA) ---

    @Test
    fun `이동수단 오버로드도 거리 0 이하면 null이다`() {
        assertNull(calculateEta(distanceMeters = 0f, mode = TravelMode.WALK))
        assertNull(calculateEta(distanceMeters = -10f, mode = TravelMode.TRANSIT))
    }

    @Test
    fun `도보는 우회 계수를 반영해 직선거리보다 더 걸린다`() {
        // 1000m 직선 → ×1.3 = 1300m 경로, 4.5km/h(=1.25m/s) → 1040초 = 17.3분 → 17분
        assertEquals(17, calculateEta(distanceMeters = 1000f, mode = TravelMode.WALK))
    }

    @Test
    fun `대중교통은 순항 속도에 고정 대기시간을 더한다`() {
        // 5000m 직선 → ×1.3 = 6500m 경로, 18km/h(=5m/s) → 1300초 = 21.6분 → 21분, +6분 대기 = 27분
        assertEquals(27, calculateEta(distanceMeters = 5000f, mode = TravelMode.TRANSIT))
    }

    @Test
    fun `같은 거리라면 대중교통이 도보보다 빠르다`() {
        val walk = calculateEta(distanceMeters = 5000f, mode = TravelMode.WALK)!!
        val transit = calculateEta(distanceMeters = 5000f, mode = TravelMode.TRANSIT)!!
        assertTrue("대중교통($transit) < 도보($walk)", transit < walk)
    }

    @Test
    fun `아주 가까워도 대중교통은 최소 이동 1분에 고정 대기가 더해진다`() {
        // 10m라도 이동 최소 1분 + 대기 6분 = 7분
        assertEquals(7, calculateEta(distanceMeters = 10f, mode = TravelMode.TRANSIT))
    }
}
