package com.kero.meetpin.core.data.repository

import com.kero.meetpin.core.model.GroupStatus
import com.kero.meetpin.core.model.InviteStatus
import com.kero.meetpin.core.model.PinLocation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [FakeMeetPinRepository]의 상태 전이 계약을 고정하는 단위 테스트.
 *
 * 실제 서버 구현체로 교체하더라도 이 계약(전원 승낙 → ACTIVE, 도착 멱등 등)은
 * 동일하게 유지되어야 하므로, 여기서 회귀를 방어한다.
 */
class FakeMeetPinRepositoryTest {

    private val repository = FakeMeetPinRepository()

    private fun pin(placeName: String = "강남역 11번 출구") = PinLocation(
        placeName = placeName,
        latitude = 37.497942,
        longitude = 127.027621
    )

    // --- createGroup ---

    @Test
    fun `그룹 생성 시 LOBBY 상태로 시작한다`() = runTest {
        val group = repository.createGroup(pin()).getOrThrow()
        assertEquals(GroupStatus.LOBBY, group.status)
    }

    @Test
    fun `그룹 생성 시 호스트는 승낙 완료 친구 2명은 대기 상태다`() = runTest {
        val group = repository.createGroup(pin()).getOrThrow()

        assertEquals(3, group.participants.size)
        val accepted = group.participants.count { it.inviteStatus == InviteStatus.ACCEPTED }
        val pending = group.participants.count { it.inviteStatus == InviteStatus.PENDING }
        assertEquals(1, accepted) // 호스트
        assertEquals(2, pending)  // 친구 2명
    }

    @Test
    fun `장소명이 있으면 제목으로 쓰고 비어 있으면 기본 문구를 쓴다`() = runTest {
        val named = repository.createGroup(pin(placeName = "홍대입구")).getOrThrow()
        assertEquals("홍대입구", named.title)

        val blank = repository.createGroup(pin(placeName = "")).getOrThrow()
        assertEquals("여기서 만나요", blank.title)
    }

    @Test
    fun `그룹을 여러 개 만들면 id와 초대코드가 서로 다르다`() = runTest {
        val first = repository.createGroup(pin()).getOrThrow()
        val second = repository.createGroup(pin()).getOrThrow()

        assertTrue(first.id != second.id)
        assertTrue(first.inviteCode != second.inviteCode)
    }

    // --- getGroupByInviteCode ---

    @Test
    fun `초대코드로 생성한 그룹을 조회할 수 있다`() = runTest {
        val created = repository.createGroup(pin()).getOrThrow()
        val found = repository.getGroupByInviteCode(created.inviteCode).getOrThrow()
        assertEquals(created.id, found.id)
    }

    @Test
    fun `존재하지 않는 초대코드는 실패를 반환한다`() = runTest {
        val result = repository.getGroupByInviteCode("UNKNOWN")
        assertTrue(result.isFailure)
    }

    // --- updateInviteStatus ---

    @Test
    fun `대기자 전원이 승낙하면 그룹이 ACTIVE로 전이된다`() = runTest {
        val group = repository.createGroup(pin()).getOrThrow()

        repository.updateInviteStatus(group.id, InviteStatus.ACCEPTED).getOrThrow()

        val updated = repository.observeGroup(group.id).first()
        assertTrue(updated.isAllAccepted)
        assertEquals(GroupStatus.ACTIVE, updated.status)
    }

    @Test
    fun `대기자가 거절하면 전원 승낙이 아니므로 LOBBY에 머문다`() = runTest {
        val group = repository.createGroup(pin()).getOrThrow()

        repository.updateInviteStatus(group.id, InviteStatus.DECLINED).getOrThrow()

        val updated = repository.observeGroup(group.id).first()
        assertFalse(updated.isAllAccepted)
        assertEquals(GroupStatus.LOBBY, updated.status)
    }

    @Test
    fun `존재하지 않는 그룹의 초대 상태 갱신은 실패한다`() = runTest {
        val result = repository.updateInviteStatus("group-does-not-exist", InviteStatus.ACCEPTED)
        assertTrue(result.isFailure)
    }

    // --- reportArrival ---

    @Test
    fun `도착 보고 시 해당 참가자만 도착 처리되고 도착 시각이 기록된다`() = runTest {
        val group = repository.createGroup(pin()).getOrThrow()
        val target = group.participants.first()

        repository.reportArrival(group.id, target.userId).getOrThrow()

        val updated = repository.observeGroup(group.id).first()
        val arrived = updated.participants.first { it.userId == target.userId }
        assertTrue(arrived.isArrived)
        assertNotNull(arrived.arrivedAt)

        val others = updated.participants.filter { it.userId != target.userId }
        assertTrue(others.none { it.isArrived })
    }

    @Test
    fun `이미 도착한 참가자의 재보고는 도착 시각을 덮어쓰지 않는다`() = runTest {
        val group = repository.createGroup(pin()).getOrThrow()
        val userId = group.participants.first().userId

        repository.reportArrival(group.id, userId).getOrThrow()
        val firstArrivedAt = repository.observeGroup(group.id).first()
            .participants.first { it.userId == userId }.arrivedAt

        repository.reportArrival(group.id, userId).getOrThrow()
        val secondArrivedAt = repository.observeGroup(group.id).first()
            .participants.first { it.userId == userId }.arrivedAt

        assertEquals(firstArrivedAt, secondArrivedAt)
    }

    @Test
    fun `도착 보고는 그룹 상태를 바꾸지 않는다`() = runTest {
        val group = repository.createGroup(pin()).getOrThrow()
        val statusBefore = group.status

        repository.reportArrival(group.id, group.participants.first().userId).getOrThrow()

        val updated = repository.observeGroup(group.id).first()
        assertEquals(statusBefore, updated.status)
    }

    @Test
    fun `존재하지 않는 그룹의 도착 보고는 실패한다`() = runTest {
        val result = repository.reportArrival("group-does-not-exist", "user-host")
        assertTrue(result.isFailure)
    }

    // --- observeGroup ---

    @Test
    fun `observeGroup은 갱신된 최신 그룹 상태를 반영한다`() = runTest {
        val group = repository.createGroup(pin()).getOrThrow()
        assertEquals(GroupStatus.LOBBY, repository.observeGroup(group.id).first().status)

        repository.updateInviteStatus(group.id, InviteStatus.ACCEPTED).getOrThrow()
        assertEquals(GroupStatus.ACTIVE, repository.observeGroup(group.id).first().status)
    }

    @Test
    fun `초기 상태에서는 어떤 참가자도 도착하지 않은 상태다`() = runTest {
        val group = repository.createGroup(pin()).getOrThrow()
        assertTrue(group.participants.none { it.isArrived })
        assertNull(group.participants.first().arrivedAt)
    }
}
