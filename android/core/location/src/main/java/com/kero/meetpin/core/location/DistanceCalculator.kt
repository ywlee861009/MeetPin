package com.kero.meetpin.core.location

import android.location.Location

/**
 * 두 좌표 사이의 거리(미터)를 계산하는 seam.
 *
 * 실제 지오데시(위경도 → 미터) 계산은 플랫폼 구현([AndroidDistanceCalculator])에 맡기고,
 * 단위 테스트에서는 결정론적 Fake를 주입해 [ArrivalDetector]의 "판정 로직"(임계값 비교·재발화
 * 억제)만 검증한다. 실제 거리 계산의 정확성은 실기기 계측/E2E 테스트의 몫이다.
 *
 * 이렇게 분리하면 각 테스트 층이 그 층에서만 검증 가능한 것을 검증하게 되어,
 * 순수 JVM 단위 테스트와 실기기 동작 사이의 정합성 틈이 생기지 않는다.
 */
fun interface DistanceCalculator {
    fun distanceMeters(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Float
}

/**
 * Android [Location.distanceBetween] 기반 거리 계산기(프로덕션 기본값).
 *
 * WGS84 타원체 기반이라 구체 근사(Haversine)보다 정확하다. Android 프레임워크에
 * 의존하므로 순수 JVM 단위 테스트에서는 실행되지 않는다(`Method not mocked`).
 * 실제 계산 정확성은 실기기/계측 테스트에서 검증한다.
 */
object AndroidDistanceCalculator : DistanceCalculator {
    override fun distanceMeters(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(startLat, startLng, endLat, endLng, results)
        return results[0]
    }
}
