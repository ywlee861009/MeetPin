package com.kero.meetpin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kero.meetpin.core.designsystem.theme.MeetPinTextStyles
import com.kero.meetpin.core.designsystem.theme.MeetPinTheme

/**
 * 초대코드 블록 — 넓은 자간의 모노 코드 + 복사 버튼. 점선 테두리로 "복사 대상"임을 암시.
 */
@Composable
fun InviteCodeBox(
    code: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val radius = 14.dp
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MeetPinTheme.extendedColors.codeSurface)
            .dashedBorder(color = MeetPinTheme.extendedColors.border, cornerRadius = radius)
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = code,
            style = MeetPinTextStyles.code,
            color = MeetPinTheme.extendedColors.ink,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        MeetPinSecondaryButton(text = "복사", onClick = onCopy)
    }
}
