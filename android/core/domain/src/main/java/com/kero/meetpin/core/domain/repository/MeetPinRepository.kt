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

    /**
     * 지정 참가자가 약속 장소에 도착했음을 보고한다.
     * 해당 참가자의 도착 상태(`isArrived`)를 갱신해 도착 시각화(체크마크·"도착 완료!" 라벨)를 켠다.
     * 이미 도착한 참가자에 대한 재보고는 무시한다(멱등).
     */
    suspend fun reportArrival(groupId: String, userId: String): Result<Unit>

    fun observeGroup(groupId: String): Flow<MeetPinGroup>
}
