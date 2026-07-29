package com.kero.meetpin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kero.meetpin.core.designsystem.theme.MeetPinTheme
import com.kero.meetpin.core.designsystem.theme.PillShape

/**
 * 참가자/공유 상태를 색과 형태 양쪽으로 드러내는 필(pill).
 *
 * - [Moving]  : 강조색 약한 배경 — 이동 중
 * - [Arrived] : 도착 시맨틱 — 도착
 * - [Sharing] : 뉴트럴 + 라이브 도트 — 공유 중
 */
enum class MeetPinStatus { Moving, Arrived, Sharing }

@Composable
fun StatusPill(
    status: MeetPinStatus,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val bg: Color
    val fg: Color
    val text: String
    val pulsing: Boolean

    when (status) {
        MeetPinStatus.Moving -> {
            bg = MaterialTheme.colorScheme.primaryContainer
            fg = MaterialTheme.colorScheme.onPrimaryContainer
            text = label ?: "이동 중"
            pulsing = false
        }
        MeetPinStatus.Arrived -> {
            bg = MeetPinTheme.extendedColors.arrivedContainer
            fg = MeetPinTheme.extendedColors.onArrivedContainer
            text = label ?: "도착"
            pulsing = false
        }
        MeetPinStatus.Sharing -> {
            bg = MaterialTheme.colorScheme.surfaceVariant
            fg = MeetPinTheme.extendedColors.ink
            text = label ?: "공유 중"
            pulsing = true
        }
    }

    Row(
        modifier = modifier
            .clip(PillShape)
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (pulsing) {
            PulsingDot(color = MaterialTheme.colorScheme.primary, size = 8.dp)
        } else {
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(fg),
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}
