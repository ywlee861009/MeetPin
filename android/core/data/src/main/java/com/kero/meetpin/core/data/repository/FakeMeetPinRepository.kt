package com.kero.meetpin.core.data.repository

import com.kero.meetpin.core.domain.repository.MeetPinRepository
import com.kero.meetpin.core.model.GroupStatus
import com.kero.meetpin.core.model.InviteStatus
import com.kero.meetpin.core.model.MeetPinGroup
import com.kero.meetpin.core.model.Participant
import com.kero.meetpin.core.model.PinLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 서버 API가 준비되기 전까지 사용하는 인메모리 [MeetPinRepository] 구현체.
 *
 * **이 클래스는 Fake입니다.** 프로세스가 종료되면 모든 그룹 데이터가 사라지며,
 * 네트워크 통신을 하지 않습니다. 실제 API 연동 시에는 `:core:network` 기반의
 * 구현체를 새로 작성하고 `DataModule`의 바인딩만 교체하면 됩니다.
 *
 * 단일 기기에서 전체 플로우(핀 생성 → 초대 → 대기실 → 트래킹)를 시연할 수 있도록,
 * 그룹 생성 시 호스트(승낙 완료)와 초대받은 참가자 1명(승낙 대기)을 함께 만든다.
 * [updateInviteStatus]로 대기 중인 참가자가 승낙하면 전원 승낙이 되어
 * 그룹 상태가 [GroupStatus.ACTIVE]로 전이된다.
 */
@Singleton
class FakeMeetPinRepository @Inject constructor() : MeetPinRepository {

    private val groups = MutableStateFlow<Map<String, MeetPinGroup>>(emptyMap())
    private val sequence = AtomicInteger(0)

    override suspend fun createGroup(
        location: PinLocation
    ): Result<MeetPinGroup> {
        val seq = sequence.incrementAndGet()
        val group = MeetPinGroup(
            id = "group-$seq",
            // 제목 입력을 받지 않는다. 장소명이 있으면 쓰고, 없으면 기본 문구.
            title = location.placeName.ifBlank { "여기서 만나요" },
            hostId = HOST_USER_ID,
            pinLocation = location,
            // 일시 개념 제거 — 생성 시각을 그대로 둔다.
            scheduledAt = System.currentTimeMillis(),
            status = GroupStatus.LOBBY,
            inviteCode = "MP%04d".format(seq),
            participants = listOf(
                Participant(
                    userId = HOST_USER_ID,
                    nickname = "나 (호스트)",
                    inviteStatus = InviteStatus.ACCEPTED
                ),
                Participant(
                    userId = GUEST_USER_ID,
                    nickname = "초대받은 친구",
                    inviteStatus = InviteStatus.PENDING
                )
            )
        )
        groups.update { it + (group.id to group) }
        return Result.success(group)
    }

    override suspend fun getGroupByInviteCode(inviteCode: String): Result<MeetPinGroup> {
        val group = groups.value.values.find { it.inviteCode == inviteCode }
            ?: return Result.failure(NoSuchElementException("초대 코드를 찾을 수 없습니다: $inviteCode"))
        return Result.success(group)
    }

    /**
     * 승낙 대기(PENDING) 중인 참가자의 상태를 갱신한다.
     * 전원이 승낙하면 그룹 상태를 [GroupStatus.ACTIVE]로 전이시켜
     * 대기실이 라이브 트래킹으로 넘어갈 수 있게 한다.
     */
    override suspend fun updateInviteStatus(
        groupId: String,
        status: InviteStatus
    ): Result<Unit> {
        val group = groups.value[groupId]
            ?: return Result.failure(NoSuchElementException("그룹을 찾을 수 없습니다: $groupId"))

        val updatedParticipants = group.participants.map { participant ->
            if (participant.inviteStatus == InviteStatus.PENDING) {
                participant.copy(inviteStatus = status)
            } else {
                participant
            }
        }

        val updatedGroup = group.copy(participants = updatedParticipants).let {
            if (it.isAllAccepted) it.copy(status = GroupStatus.ACTIVE) else it
        }

        groups.update { current -> current + (groupId to updatedGroup) }
        return Result.success(Unit)
    }

    override fun observeGroup(groupId: String): Flow<MeetPinGroup> =
        groups
            .map { it[groupId] }
            .filterNotNull()
            .distinctUntilChanged()

    private companion object {
        const val HOST_USER_ID = "user-host"
        const val GUEST_USER_ID = "user-guest"
    }
}
