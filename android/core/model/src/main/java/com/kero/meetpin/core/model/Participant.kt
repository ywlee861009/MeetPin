package com.kero.meetpin.core.model

data class Participant(
    val userId: String,
    val nickname: String,
    val profileImageUrl: String? = null,
    val inviteStatus: InviteStatus = InviteStatus.PENDING,
    val isArrived: Boolean = false,
    val arrivedAt: Long? = null
)
