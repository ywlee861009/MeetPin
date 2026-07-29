package com.kero.meetpin.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kero.meetpin.core.designsystem.theme.MeetPinTextStyles
import com.kero.meetpin.core.designsystem.theme.MeetPinTheme

/**
 * 참가자 상태 행 — 아바타 · 이름/상태 · 우측 거리(모노).
 *
 * @param avatarColor 이동 중 아바타 배경(도착이면 무시되고 도착 시맨틱 컬러 사용).
 * @param avatarContent 프로필 이미지 등 아바타 커스텀 슬롯(있으면 이니셜 대신 사용).
 */
@Composable
fun ParticipantRow(
    name: String,
    statusLabel: String,
    distanceLabel: String,
    isArrived: Boolean,
    modifier: Modifier = Modifier,
    avatarColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    avatarContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick: (() -> Unit)? = null,
    avatarContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MeetPinAvatar(
            initial = name,
            backgroundColor = avatarColor,
            contentColor = avatarContentColor,
            isArrived = isArrived,
            content = avatarContent,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = MeetPinTheme.extendedColors.ink,
            )
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isArrived) {
                    MeetPinTheme.extendedColors.arrived
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = distanceLabel,
            style = MeetPinTextStyles.data,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
