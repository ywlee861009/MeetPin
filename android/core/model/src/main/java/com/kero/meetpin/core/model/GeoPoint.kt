package com.kero.meetpin.core.model

/**
 * 지도 벤더에 독립적인 좌표값.
 *
 * Google/Kakao/Naver 등 특정 지도 SDK의 `LatLng` 타입이 도메인·프레젠테이션 계층으로
 * 새어나오지 않도록 하는 중립 좌표 타입이다. state·intent·effect·usecase는 이 타입만 사용하고,
 * 벤더 타입 변환은 `:core:map-<vendor>` 구현 안에서만 일어난다.
 */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)
