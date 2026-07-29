package com.kero.meetpin.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kero.meetpin.core.designsystem.theme.MeetPinTheme

/**
 * 컴포넌트 갤러리 프리뷰. Android Studio에서 라이트/다크를 나란히 확인하기 위한 개발용.
 */
@Composable
private fun Gallery() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("Buttons", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            MeetPinButton(text = "여기로 약속 만들기", onClick = {}, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MeetPinSecondaryButton(text = "초대 링크 공유", onClick = {})
                MeetPinGhostButton(text = "공유 중지", onClick = {})
            }

            Text("Status", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(MeetPinStatus.Moving)
                StatusPill(MeetPinStatus.Arrived)
                StatusPill(MeetPinStatus.Sharing)
            }
            LiveBadge()

            Text("Invite code", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            InviteCodeBox(code = "7K4-QP9", onCopy = {}, modifier = Modifier.fillMaxWidth())

            Text("Chat", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MeetPinChatBubble(text = "어디쯤이야?", isMine = false)
                MeetPinChatBubble(text = "거의 다 왔어! 🏃", isMine = true)
            }

            Text("Participants", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ParticipantRow(
                name = "나",
                statusLabel = "이동 중",
                distanceLabel = "320 m",
                isArrived = false,
                avatarColor = MaterialTheme.colorScheme.primary,
                avatarContentColor = MaterialTheme.colorScheme.onPrimary,
            )
            ParticipantRow(
                name = "지훈",
                statusLabel = "도착함",
                distanceLabel = "0 m",
                isArrived = true,
            )

            Text("Pins", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PinMarker(color = MaterialTheme.colorScheme.primary)
                PinMarker(color = MeetPinTheme.extendedColors.ink)
                MeetPinAvatar(initial = "지", backgroundColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                MeetPinAvatar(initial = "지훈", isArrived = true)
            }
        }
    }
}

@Preview(name = "Light", showBackground = true, widthDp = 380)
@Composable
private fun GalleryLightPreview() {
    MeetPinTheme(darkTheme = false) { Gallery() }
}

@Preview(name = "Dark", showBackground = true, widthDp = 380, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GalleryDarkPreview() {
    MeetPinTheme(darkTheme = true) { Gallery() }
}
