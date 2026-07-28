package com.kero.meetpin.core.model

data class MeetPinGroup(
    val id: String,
    val title: String,
    val hostId: String,
    val pinLocation: PinLocation,
    val scheduledAt: Long,
    val status: GroupStatus = GroupStatus.LOBBY,
    val inviteCode: String,
    val participants: List<Participant> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val isAllAccepted: Boolean
        get() = participants.isNotEmpty() && participants.all { it.inviteStatus == InviteStatus.ACCEPTED }
}
