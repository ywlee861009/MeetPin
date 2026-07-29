package com.kero.meetpin.feature.tracking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kero.meetpin.core.designsystem.component.LiveBadge
import com.kero.meetpin.core.designsystem.component.MeetPinGhostButton
import com.kero.meetpin.core.designsystem.component.ParticipantRow

/**
 * 하단 참가자 상태 시트.
 *
 * 멤버별 약속 장소까지의 남은 거리(m/km) 및 예상 소요시간(ETA)을 DS [ParticipantRow]로 표시.
 * 멤버 클릭 시 해당 마커 위치로 지도 카메라 이동.
 */
@Composable
fun ParticipantStatusSheet(
    participantMarkers: List<ParticipantMarker>,
    participantCount: Int,
    onParticipantClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "참가자 현황",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "👥 ${participantCount}명 공유 중",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 참가자 상태 리스트
        LazyColumn {
            items(
                items = participantMarkers,
                key = { it.participant.userId }
            ) { marker ->
                val participant = marker.participant
                ParticipantRow(
                    name = participant.nickname,
                    statusLabel = if (participant.isArrived) {
                        "도착 완료!"
                    } else {
                        marker.etaMinutes?.let { "이동 중 · ${formatEta(it)}" } ?: "이동 중..."
                    },
                    distanceLabel = if (participant.isArrived) {
                        ""
                    } else {
                        formatDistance(marker.distanceToPin)
                    },
                    isArrived = participant.isArrived,
                    onClick = { onParticipantClick(participant.userId) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 상단 라이브 공유 안내 바.
 *
 * 공유 중이면 DS [LiveBadge](맥동 도트)와 끄기 액션을, 중지 상태면 안내 문구를 보여준다.
 */
@Composable
fun LiveSharingTopBar(
    isActive: Boolean,
    onStopSharing: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isActive) {
            LiveBadge(text = "실시간 위치 공유 중")
            MeetPinGhostButton(
                text = "공유 끄기",
                onClick = onStopSharing
            )
        } else {
            Text(
                text = "⏹ 위치 공유 중지됨",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * ETA(예상 소요시간)를 사람이 읽기 쉬운 형태로 포맷한다.
 */
fun formatEta(minutes: Int): String {
    return when {
        minutes < 1 -> "곧 도착"
        minutes < 60 -> "약 ${minutes}분"
        else -> {
            val hours = minutes / 60
            val mins = minutes % 60
            if (mins > 0) "약 ${hours}시간 ${mins}분"
            else "약 ${hours}시간"
        }
    }
}
