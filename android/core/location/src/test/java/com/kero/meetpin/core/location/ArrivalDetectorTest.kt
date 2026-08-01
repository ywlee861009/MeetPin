package com.kero.meetpin.core.location

import com.kero.meetpin.core.model.PinLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [ArrivalDetector]의 "판정 로직" 단위 테스트.
 *
 * 실제 거리 계산(지오데시)은 [DistanceCalculator] seam에 결정론적 Fake를 주입해 대체한다.
 * 여기서 검증하는 것은 거리 자체의 정확도가 아니라 다음 계약이다.
 *  - 반경 임계값 비교(경계 포함 여부)
 *  - 커스텀 반경 반영
 *  - 이미 도착한 경우 재발화하지 않음(멱등) 및 불필요한 거리 계산 생략
 *
 * 실제 좌표 → 미터 계산의 정확성은 실기기 계측/E2E 테스트에서 검증한다.
 */
class ArrivalDetectorTest {

    /** 항상 [distance]를 반환하고, 호출 횟수를 세는 Fake 거리 계산기. */
    private class FakeDistanceCalculator(var distance: Float) : DistanceCalculator {
        var callCount: Int = 0
            private set

        override fun distanceMeters(
            startLat: Double,
            startLng: Double,
            endLat: Double,
            endLng: Double
        ): Float {
            callCount++
            return distance
        }
    }

    private val pin = PinLocation(
        placeName = "목적지",
        latitude = 37.5,
        longitude = 127.0
    )

    private fun detector(distance: Float, radius: Float = 50f): Pair<ArrivalDetector, FakeDistanceCalculator> {
        val fake = FakeDistanceCalculator(distance)
        return ArrivalDetector(arrivalRadiusMeters = radius, distanceCalculator = fake) to fake
    }

    // --- isWithinRadius ---

    @Test
    fun `반경보다 가까우면 반경 내로 판정한다`() {
        val (detector, _) = detector(distance = 30f, radius = 50f)
        assertTrue(detector.isWithinRadius(0.0, 0.0, pin))
    }

    @Test
    fun `반경보다 멀면 반경 밖으로 판정한다`() {
        val (detector, _) = detector(distance = 80f, radius = 50f)
        assertFalse(detector.isWithinRadius(0.0, 0.0, pin))
    }

    @Test
    fun `정확히 반경 경계면 도착으로 본다(경계 포함)`() {
        val (detector, _) = detector(distance = 50f, radius = 50f)
        assertTrue(detector.isWithinRadius(0.0, 0.0, pin))
    }

    @Test
    fun `커스텀 반경을 적용한다`() {
        // 거리 80m: 기본 반경 50m라면 밖이지만, 반경 100m면 안쪽이다.
        val (wide, _) = detector(distance = 80f, radius = 100f)
        assertTrue(wide.isWithinRadius(0.0, 0.0, pin))

        val (narrow, _) = detector(distance = 80f, radius = 50f)
        assertFalse(narrow.isWithinRadius(0.0, 0.0, pin))
    }

    // --- calculateDistance ---

    @Test
    fun `calculateDistance는 주입된 계산기 값을 그대로 반환한다`() {
        val (detector, _) = detector(distance = 123.4f)
        assertEquals(123.4f, detector.calculateDistance(0.0, 0.0, 1.0, 1.0), 0.0001f)
    }

    // --- detectArrival ---

    @Test
    fun `미도착 상태에서 반경에 진입하면 도착 이벤트가 발생한다`() {
        val (detector, _) = detector(distance = 30f, radius = 50f)
        assertTrue(detector.detectArrival(wasArrived = false, 0.0, 0.0, pin))
    }

    @Test
    fun `미도착 상태에서 반경 밖이면 도착 이벤트가 없다`() {
        val (detector, _) = detector(distance = 80f, radius = 50f)
        assertFalse(detector.detectArrival(wasArrived = false, 0.0, 0.0, pin))
    }

    @Test
    fun `이미 도착한 상태면 반경 안이라도 재발화하지 않는다`() {
        val (detector, _) = detector(distance = 10f, radius = 50f)
        assertFalse(detector.detectArrival(wasArrived = true, 0.0, 0.0, pin))
    }

    @Test
    fun `이미 도착한 상태면 거리 계산조차 하지 않는다`() {
        val (detector, fake) = detector(distance = 10f, radius = 50f)
        detector.detectArrival(wasArrived = true, 0.0, 0.0, pin)
        assertEquals(0, fake.callCount)
    }
}
