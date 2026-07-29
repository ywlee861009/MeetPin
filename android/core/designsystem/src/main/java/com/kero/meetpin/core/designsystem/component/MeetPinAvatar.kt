package com.kero.meetpin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import com.kero.meetpin.core.designsystem.theme.MeetPinTheme

/**
 * 참가자 아바타.
 *
 * - 기본: [backgroundColor] 위에 이름 이니셜.
 * - [isArrived] = true: 도착 시맨틱 컬러 배경 + 체크 표시(이니셜 대신).
 *
 * 프로필 이미지는 DS가 이미지 로더에 의존하지 않도록 [content] 슬롯으로 위임한다.
 * (feature에서 Coil AsyncImage를 넣는다.)
 */
@Composable
fun MeetPinAvatar(
    initial: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    isArrived: Boolean = false,
    ringColor: Color = MaterialTheme.colorScheme.surface,
    content: (@Composable () -> Unit)? = null,
) {
    val bg = if (isArrived) MeetPinTheme.extendedColors.arrived else backgroundColor
    val fg = if (isArrived) MeetPinTheme.extendedColors.onArrived else contentColor

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .border(2.dp, ringColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        when {
            content != null -> content()
            isArrived -> CheckMark(color = fg, modifier = Modifier.size(size * 0.42f))
            else -> Text(
                text = initial.take(1),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = fg,
            )
        }
    }
}

/** 의존성 없이 Canvas로 그린 체크 표시. */
@Composable
fun CheckMark(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(16.dp)) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.16f
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w * 0.16f, h * 0.55f),
            end = androidx.compose.ui.geometry.Offset(w * 0.40f, h * 0.78f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w * 0.40f, h * 0.78f),
            end = androidx.compose.ui.geometry.Offset(w * 0.82f, h * 0.24f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}
