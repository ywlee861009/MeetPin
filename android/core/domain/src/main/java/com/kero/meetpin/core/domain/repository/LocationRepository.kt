package com.kero.meetpin.core.domain.repository

import com.kero.meetpin.core.model.LocationUpdate
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getCurrentLocation(): Flow<LocationUpdate>
    fun observeGroupLocations(groupId: String): Flow<List<LocationUpdate>>
    suspend fun sendLocation(locationUpdate: LocationUpdate): Result<Unit>
}
