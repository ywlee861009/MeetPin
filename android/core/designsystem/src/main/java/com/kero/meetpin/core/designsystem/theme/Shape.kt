package com.kero.meetpin.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 모양 토큰. 모던 미니멀 — 라운드는 "살짝".
 *
 * - extraSmall(8) : 칩/작은 배지
 * - small(10)     : 인풋/작은 카드
 * - medium(14)    : 기본 카드
 * - large(20)     : 큰 카드/맵 카드
 * - extraLarge(28): 바텀시트 상단
 */
val MeetPinShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** 완전한 알약형(버튼·필·뱃지). */
val PillShape = RoundedCornerShape(percent = 50)

/** 바텀시트 — 상단만 둥글게. */
val BottomSheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
