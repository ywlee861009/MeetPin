package com.kero.meetpin.core.map.google

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.kero.meetpin.core.map.MapCameraState
import com.kero.meetpin.core.map.MapMarkerScope
import com.kero.meetpin.core.map.MapRenderer
import com.kero.meetpin.core.map.MeetPinMapUiSettings
import com.kero.meetpin.core.model.GeoPoint
import com.google.maps.android.compose.MapUiSettings as GoogleMapUiSettings
import com.google.maps.android.compose.Marker as GoogleMarker
import com.google.maps.android.compose.MarkerState as GoogleMarkerState

internal fun GeoPoint.toLatLng() = LatLng(latitude, longitude)
internal fun LatLng.toGeoPoint() = GeoPoint(latitude, longitude)

/**
 * Google Maps 기반 [MapCameraState] 구현.
 */
class GoogleMapCameraState(
    val delegate: CameraPositionState,
) : MapCameraState {

    override var position: GeoPoint
        get() = delegate.position.target.toGeoPoint()
        set(value) {
            delegate.position =
                CameraPosition.fromLatLngZoom(value.toLatLng(), delegate.position.zoom)
        }

    override suspend fun animate(target: GeoPoint, zoom: Float?, durationMs: Int) {
        val update = if (zoom != null) {
            CameraUpdateFactory.newLatLngZoom(target.toLatLng(), zoom)
        } else {
            CameraUpdateFactory.newLatLng(target.toLatLng())
        }
        if (durationMs > 0) {
            delegate.animate(update, durationMs)
        } else {
            delegate.animate(update)
        }
    }

    override fun toScreenOffset(point: GeoPoint): Offset? {
        val projection = delegate.projection ?: return null
        val screenPoint = projection.toScreenLocation(point.toLatLng())
        return Offset(screenPoint.x.toFloat(), screenPoint.y.toFloat())
    }
}

/**
 * Google Maps Compose 기반 [MapRenderer] 구현.
 *
 * 구글 SDK 타입은 이 모듈 밖으로 절대 노출하지 않는다.
 */
class GoogleMapRenderer : MapRenderer {

    @Composable
    override fun rememberCameraState(
        initialPosition: GeoPoint,
        initialZoom: Float,
    ): MapCameraState {
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(initialPosition.toLatLng(), initialZoom)
        }
        return remember(cameraPositionState) { GoogleMapCameraState(cameraPositionState) }
    }

    @Composable
    override fun Map(
        modifier: Modifier,
        cameraState: MapCameraState,
        myLocationEnabled: Boolean,
        uiSettings: MeetPinMapUiSettings,
        contentPadding: PaddingValues,
        onMapClick: ((GeoPoint) -> Unit)?,
        content: @Composable MapMarkerScope.() -> Unit,
    ) {
        val google = cameraState as GoogleMapCameraState
        GoogleMap(
            modifier = modifier,
            cameraPositionState = google.delegate,
            contentPadding = contentPadding,
            properties = MapProperties(
                isMyLocationEnabled = myLocationEnabled,
                mapType = MapType.NORMAL,
            ),
            uiSettings = GoogleMapUiSettings(
                zoomControlsEnabled = uiSettings.zoomControlsEnabled,
                myLocationButtonEnabled = uiSettings.myLocationButtonEnabled,
                compassEnabled = uiSettings.compassEnabled,
                mapToolbarEnabled = false,
            ),
            onMapClick = { latLng -> onMapClick?.invoke(latLng.toGeoPoint()) },
        ) {
            GoogleMapMarkerScope.content()
        }
    }
}

/**
 * Google Maps content 스코프 안에서 마커를 배치하는 [MapMarkerScope] 구현.
 *
 * 마커 컴포저블은 반드시 [GoogleMap] content 람다 안에서 호출되므로,
 * 이 object의 메서드도 그 안에서만 호출된다.
 */
private object GoogleMapMarkerScope : MapMarkerScope {

    @Composable
    override fun Marker(position: GeoPoint, title: String?, snippet: String?) {
        GoogleMarker(
            state = GoogleMarkerState(position = position.toLatLng()),
            title = title,
            snippet = snippet,
        )
    }

    @Composable
    override fun CustomMarker(
        key: String,
        position: GeoPoint,
        title: String?,
        snippet: String?,
        contentKey: Any,
        content: @Composable () -> Unit,
    ) {
        val markerState = rememberMarkerState(key = key, position = position.toLatLng())
        markerState.position = position.toLatLng()
        // contentKey가 바뀔 때만 마커 비트맵을 다시 굽는다. (말풍선 등장/소멸 반영)
        MarkerComposable(
            contentKey,
            state = markerState,
            title = title ?: "",
            snippet = snippet ?: "",
        ) {
            content()
        }
    }
}
