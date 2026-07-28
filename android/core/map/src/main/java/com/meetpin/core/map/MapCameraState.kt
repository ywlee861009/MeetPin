package com.meetpin.core.map

import androidx.compose.ui.geometry.Offset
import com.meetpin.core.model.GeoPoint

/**
 * 지도 카메라 상태 홀더 (벤더 중립).
 *
 * 벤더별 카메라 상태(`CameraPositionState` 등)를 감싸며, 화면은 이 인터페이스만 통해
 * 카메라를 읽고 조작한다.
 */
interface MapCameraState {
    /** 현재 카메라 중심 좌표 */
    var position: GeoPoint

    /**
     * 카메라를 [target]으로 (선택적으로 [zoom] 레벨까지) 이동한다.
     * @param durationMs 0보다 크면 애니메이션, 0이면 즉시 이동
     */
    suspend fun animate(target: GeoPoint, zoom: Float? = null, durationMs: Int = 0)

    /**
     * 지도 좌표를 화면 픽셀 오프셋으로 변환한다.
     *
     * 지도가 아직 그려지지 않았거나 SDK가 지원하지 않으면 `null`.
     * 오프스크린 오버레이처럼 벤더별 구현 차이가 큰 기능을 위한 escape hatch다.
     */
    fun toScreenOffset(point: GeoPoint): Offset?
}
