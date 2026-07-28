package com.kero.meetpin.core.map

import androidx.compose.foundation.layout.PaddingValues
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
 *
 * ⚠️ 파라미터 기본값(default)을 두지 않는다 — `@Composable` 인터페이스 멤버에 default를 주면
 * 컴파일러가 만드는 `$default` 브릿지가 구현체 시그니처와 어긋나
 * 런타임 `AbstractMethodError`를 낸다. 호출부에서 명시적으로 넘긴다.
 */
interface MapMarkerScope {
    /** 기본 핀 마커 */
    @Composable
    @ComposableOpenTarget(-1)
    fun Marker(position: GeoPoint, title: String?, snippet: String?)

    /**
     * 커스텀 컴포저블을 마커로 렌더링한다 (아바타 등).
     * @param key 마커 식별 키 (위치 보간 애니메이션의 안정적 갱신용)
     * @param contentKey [content]가 만들어내는 그림이 바뀔 때마다 값이 달라져야 하는 키.
     *   일부 지도 SDK는 마커 컴포저블을 비트맵으로 한 번만 굽기 때문에, 이 값이 바뀌어야
     *   마커를 다시 그린다. (예: 말풍선 텍스트, 도착 여부.) 위치는 넣지 말 것 — 매 프레임
     *   재래스터화되어 비싸다.
     */
    @Composable
    @ComposableOpenTarget(-1)
    fun CustomMarker(
        key: String,
        position: GeoPoint,
        title: String?,
        snippet: String?,
        contentKey: Any,
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

    /**
     * 지도를 렌더링한다.
     *
     * 파라미터 기본값을 두지 않는다 (위 [MapMarkerScope]와 동일한 `AbstractMethodError` 이유).
     * 호출부에서 모든 인자를 명시적으로 넘긴다.
     *
     * @param contentPadding 지도 SDK가 그리는 네이티브 컨트롤(현위치·확대/축소 버튼, 로고 등)을
     *   안전 영역 안으로 밀어넣기 위한 패딩. 시스템 바 인셋을 넘긴다.
     */
    @Composable
    @ComposableOpenTarget(-1)
    fun Map(
        modifier: Modifier,
        cameraState: MapCameraState,
        myLocationEnabled: Boolean,
        uiSettings: MeetPinMapUiSettings,
        contentPadding: PaddingValues,
        onMapClick: ((GeoPoint) -> Unit)?,
        content: @Composable @ComposableOpenTarget(-1) MapMarkerScope.() -> Unit,
    )
}
