package com.meetpin.core.location

import com.meetpin.core.domain.repository.LocationRepository
import com.meetpin.core.model.LocationUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * LocationRepository 구현체.
 * DefaultLocationClient에서 수집한 GPS 좌표를 LocationUpdate 도메인 모델로 변환하여 Flow로 방출.
 *
 * 서버 연동(WebSocket 등)은 core:network 모듈에서 추후 구현 예정.
 * 현재는 로컬 위치 수집 파이프라인만 구현한다.
 */
class DefaultLocationRepository(
    private val locationClient: LocationClient
) : LocationRepository {

    // 그룹 위치 업데이트를 위한 SharedFlow (서버 연동 전 임시 로컬 스트림)
    private val _groupLocations = MutableSharedFlow<List<LocationUpdate>>(replay = 1)

    override fun getCurrentLocation(): Flow<LocationUpdate> {
        return locationClient.getLocationUpdates().map { location ->
            LocationUpdate(
                groupId = "",
                userId = "", // 실제 사용 시 DI를 통해 userId 주입
                latitude = location.latitude,
                longitude = location.longitude,
                bearing = if (location.hasBearing()) location.bearing else null,
                speed = if (location.hasSpeed()) location.speed else null,
                accuracy = if (location.hasAccuracy()) location.accuracy else null,
                timestamp = location.time
            )
        }
    }

    /**
     * 서버 연동 전이라 아직 아무 좌표도 수신되지 않은 상태에서도 구독자가 최초 1회는
     * 값을 받아야 한다. (트래킹 화면의 combine이 첫 emission 없이는 영구 로딩 상태가 된다.)
     * 따라서 빈 목록을 초깃값으로 방출한다.
     */
    override fun observeGroupLocations(groupId: String): Flow<List<LocationUpdate>> {
        return _groupLocations.asSharedFlow().onStart {
            if (_groupLocations.replayCache.isEmpty()) emit(emptyList())
        }
    }

    override suspend fun sendLocation(locationUpdate: LocationUpdate): Result<Unit> {
        // TODO: core:network 모듈 WebSocket 연동 후 서버 전송 구현
        return Result.success(Unit)
    }

    /**
     * 외부(서버/WebSocket)에서 수신한 그룹 위치 목록을 업데이트한다.
     */
    suspend fun updateGroupLocations(locations: List<LocationUpdate>) {
        _groupLocations.emit(locations)
    }
}
