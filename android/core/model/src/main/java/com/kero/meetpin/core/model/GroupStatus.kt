package com.kero.meetpin.core.model

enum class GroupStatus {
    LOBBY,      // 초대 진행 중 & 승낙 대기 중
    ACTIVE,     // 전원 승낙 완료 & 실시간 위치 공유 중
    FINISHED,   // 모임 완료 (위치 공유 종료)
    CANCELLED   // 취소됨
}
