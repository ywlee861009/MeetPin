package com.kero.meetpin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kero.meetpin.core.designsystem.theme.MeetPinTheme

/**
 * 채팅 말풍선.
 *
 * - [isMine] = true: 강조색 채움, 우측 정렬 꼬리(우하단 각짐).
 * - [isMine] = false: 뉴트럴 표면 + 헤어라인, 좌측 정렬 꼬리(좌하단 각짐).
 *
 * 정렬(좌/우)은 부모(Column의 Alignment)에서 결정한다.
 */
@Composable
fun MeetPinChatBubble(
    text: String,
    isMine: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = if (isMine) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }
    val bg = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (isMine) MaterialTheme.colorScheme.onPrimary else MeetPinTheme.extendedColors.ink

    val base = modifier
        .widthIn(max = 260.dp)
        .clip(shape)
        .background(bg)

    val bordered = if (isMine) base else base.border(1.dp, MeetPinTheme.extendedColors.border, shape)

    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = fg,
        modifier = bordered.padding(horizontal = 14.dp, vertical = 10.dp),
    )
}
