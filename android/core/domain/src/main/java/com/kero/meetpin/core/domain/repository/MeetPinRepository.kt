package com.kero.meetpin.core.domain.repository

import com.kero.meetpin.core.model.InviteStatus
import com.kero.meetpin.core.model.MeetPinGroup
import com.kero.meetpin.core.model.PinLocation
import kotlinx.coroutines.flow.Flow

interface MeetPinRepository {
    suspend fun createGroup(title: String, location: PinLocation, scheduledAt: Long): Result<MeetPinGroup>
    suspend fun getGroupByInviteCode(inviteCode: String): Result<MeetPinGroup>
    suspend fun updateInviteStatus(groupId: String, status: InviteStatus): Result<Unit>
    fun observeGroup(groupId: String): Flow<MeetPinGroup>
}
