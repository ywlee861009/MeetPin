package com.kero.meetpin.core.domain.repository

import com.kero.meetpin.core.model.InviteStatus
import com.kero.meetpin.core.model.MeetPinGroup
import com.kero.meetpin.core.model.PinLocation
import kotlinx.coroutines.flow.Flow

interface MeetPinRepository {
    /**
     * 핀 위치만으로 약속을 생성한다. 제목·일시는 받지 않는다 (즉시 공유 플로우).
     */
    suspend fun createGroup(location: PinLocation): Result<MeetPinGroup>
    suspend fun getGroupByInviteCode(inviteCode: String): Result<MeetPinGroup>
    suspend fun updateInviteStatus(groupId: String, status: InviteStatus): Result<Unit>
    fun observeGroup(groupId: String): Flow<MeetPinGroup>
}
