package com.kero.meetpin.core.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableOpenTarget
import androidx.compose.ui.Modifier
import com.kero.meetpin.core.model.GeoPoint

/**
 * 지도 위 마커를 배치하는 스코프 (벤더 중립).
 *
 * [MapRenderer.Map]의 content 람다 안에서만 사용한다.
 *
 * `@ComposableOpenTarget`: 이 컴포저블들은 인터페이스 멤버라 본문이 없어
 * applier-target 추론기가 스킴을 계산하지 못하고 ICE(Unknown file)를 낸다.
 * 열린(open) 타깃을 명시해 벤더별 applier에 무관하게 다형적임을 알린다.
 */
interface MapMarkerScope {
    /** 기본 핀 마커 */
    @Composable
    @ComposableOpenTarget(-1)
    fun Marker(position: GeoPoint, title: String? = null, snippet: String? = null)

    /**
     * 커스텀 컴포저블을 마커로 렌더링한다 (아바타 등).
     * @param key 마커 식별 키 (위치 보간 애니메이션의 안정적 갱신용)
     */
    @Composable
    @ComposableOpenTarget(-1)
    fun CustomMarker(
        key: String,
        position: GeoPoint,
        title: String? = null,
        snippet: String? = null,
        content: @Composable @ComposableOpenTarget(-1) () -> Unit,
    )
}

/**
 * 지도 렌더러 (벤더 중립).
 *
 * 화면은 [LocalMapRenderer]로 주입받은 구현을 통해서만 지도를 그린다.
 * 벤더 교체 시 이 인터페이스의 새 구현(`:core:map-<vendor>`)을 앱 루트에서 갈아끼운다.
 */
interface MapRenderer {
    /** 벤더별 카메라 상태를 생성·기억한다. */
    @Composable
    fun rememberCameraState(initialPosition: GeoPoint, initialZoom: Float): MapCameraState

    /** 지도를 렌더링한다. */
    @Composable
    @ComposableOpenTarget(-1)
    fun Map(
        modifier: Modifier = Modifier,
        cameraState: MapCameraState,
        myLocationEnabled: Boolean = false,
        uiSettings: MeetPinMapUiSettings = MeetPinMapUiSettings(),
        onMapClick: ((GeoPoint) -> Unit)? = null,
        content: @Composable @ComposableOpenTarget(-1) MapMarkerScope.() -> Unit = {},
    )
}
