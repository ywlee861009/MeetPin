package com.kero.meetpin.core.location

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kero.meetpin.core.model.PinLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [ArrivalDetector]의 **계측(instrumented) 테스트** — 실제 Android 런타임에서 실행된다.
 *
 * 순수 JVM 단위 테스트([ArrivalDetectorTest])는 [DistanceCalculator] Fake를 주입해
 * "판정 로직"만 검증했다. 여기서는 반대로 **기본 생성자([AndroidDistanceCalculator],
 * 즉 실제 `Location.distanceBetween`)** 를 그대로 사용해, 단위 테스트가 커버하지 못한
 * 실제 지오데시 + 도착 트리거를 실기기/에뮬레이터에서 검증한다.
 *
 * 실행: `./gradlew :core:location:connectedDebugAndroidTest` (기기/에뮬레이터 필요)
 */
@RunWith(AndroidJUnit4::class)
class ArrivalDetectorInstrumentedTest {

    // 기본 생성자 → 실제 Location.distanceBetween 사용 (반경 50m)
    private val detector = ArrivalDetector()

    private val pin = PinLocation(
        placeName = "약속 장소",
        latitude = REF_LAT,
        longitude = REF_LNG
    )

    // --- 실제 지오데시 검증 (단위 테스트가 못 하는 영역) ---

    @Test
    fun 동일_좌표의_거리는_0에_가깝다() {
        val d = detector.calculateDistance(REF_LAT, REF_LNG, REF_LAT, REF_LNG)
        assertEquals(0f, d, 0.5f)
    }

    @Test
    fun 위도_0_001도_차이는_약_111m다() {
        // 위도 1도 ≈ 111,320m 이므로 0.001도 ≈ 111.3m (위도에 거의 무관한 상수)
        val d = detector.calculateDistance(REF_LAT, REF_LNG, REF_LAT + 0.001, REF_LNG)
        assertEquals(111.3f, d, 2f)
    }

    @Test
    fun 경도_0_001도_차이는_위도_코사인만큼_짧다() {
        // 경도 1도 길이 ≈ 111,320 * cos(위도). 위도 37.5°에서 0.001도 ≈ 88.3m.
        // Fake/stub(0 반환)이나 위도를 무시하는 단순 계산으로는 통과할 수 없어,
        // 실제 지오데시가 동작함을 판별한다.
        val d = detector.calculateDistance(REF_LAT, REF_LNG, REF_LAT, REF_LNG + 0.001)
        assertEquals(88.3f, d, 2f)
    }

    // --- 접근 경로 시뮬레이션: 도착이 정확히 1번만 발화 ---

    @Test
    fun 목적지로_접근하면_도착이_정확히_한_번_발화한다() {
        // 목적지 동쪽에서 점점 다가오는 경로. 위 계산 기준(경도 0.001도 ≈ 88.3m)으로
        // 0.0005도(≈44m) 지점에서 처음 반경 50m 안으로 진입한다.
        val approachLng = listOf(
            REF_LNG + 0.0100, // ≈ 883m  (밖)
            REF_LNG + 0.0020, // ≈ 177m  (밖)
            REF_LNG + 0.0010, // ≈ 88m   (밖)
            REF_LNG + 0.0005, // ≈ 44m   (안, 최초 도착)
            REF_LNG + 0.0002, // ≈ 18m   (안)
            REF_LNG + 0.0000  // 0m      (안)
        )

        var wasArrived = false
        var arrivalCount = 0
        var firstArrivalIndex = -1

        approachLng.forEachIndexed { index, lng ->
            val justArrived = detector.detectArrival(wasArrived, REF_LAT, lng, pin)
            if (justArrived) {
                arrivalCount++
                if (firstArrivalIndex == -1) firstArrivalIndex = index
                wasArrived = true
            }
        }

        assertEquals("도착 이벤트는 정확히 한 번만 발화해야 한다", 1, arrivalCount)
        assertEquals("반경 50m에 처음 진입하는 지점(index 3)에서 발화해야 한다", 3, firstArrivalIndex)
    }

    @Test
    fun 반경_밖에_머무는_경로에서는_도착이_발화하지_않는다() {
        val outsidePath = listOf(
            REF_LNG + 0.0100, // ≈ 883m
            REF_LNG + 0.0050, // ≈ 442m
            REF_LNG + 0.0020  // ≈ 177m
        )

        var wasArrived = false
        var arrivalCount = 0
        outsidePath.forEach { lng ->
            if (detector.detectArrival(wasArrived, REF_LAT, lng, pin)) {
                arrivalCount++
                wasArrived = true
            }
        }

        assertEquals(0, arrivalCount)
        assertFalse(wasArrived)
    }

    @Test
    fun 도착_후_다시_멀어져도_재발화하지_않는다() {
        // 한 번 도착(멱등) → 이후 반경 밖으로 나가도 detectArrival은 계속 false.
        var wasArrived = detector.detectArrival(false, REF_LAT, REF_LNG, pin) // 도착
        assertTrue(wasArrived)

        val leaving = detector.detectArrival(wasArrived, REF_LAT, REF_LNG + 0.0100, pin) // ≈883m 이탈
        assertFalse(leaving)
    }

    private companion object {
        // 서울 강남 부근 임의 좌표(위도 37.5°) — 경도 코사인 효과가 뚜렷한 위도대.
        const val REF_LAT = 37.5
        const val REF_LNG = 127.0
    }
}
