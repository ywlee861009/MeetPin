package com.kero.meetpin.feature.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 하단 참가자 상태 시트.
 *
 * 멤버별 약속 장소까지의 남은 거리(m/km) 및 예상 소요시간(ETA) 카드 표시.
 * 멤버 클릭 시 해당 마커 위치로 지도 카메라 이동.
 */
@Composable
fun ParticipantStatusSheet(
    participantMarkers: List<ParticipantMarker>,
    arrivedCount: Int,
    totalCount: Int,
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
                text = "✅ $arrivedCount / $totalCount 도착",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 참가자 카드 리스트
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = participantMarkers,
                key = { it.participant.userId }
            ) { marker ->
                ParticipantStatusCard(
                    participantMarker = marker,
                    onClick = { onParticipantClick(marker.participant.userId) }
                )
            }
        }
    }
}

/**
 * 개별 참가자 상태 카드.
 *
 * - 프로필 아바타
 * - 이름
 * - 남은 거리 (m/km)
 * - 예상 소요시간 (ETA)
 * - 도착 여부
 */
@Composable
private fun ParticipantStatusCard(
    participantMarker: ParticipantMarker,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val participant = participantMarker.participant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (participant.isArrived) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 프로필 아바타
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (participant.isArrived) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (participant.isArrived) "✅" else participant.nickname.take(1),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (participant.isArrived) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 이름 + 상태
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = participant.nickname,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (participant.isArrived) {
                        "도착 완료!"
                    } else {
                        "이동 중..."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // 거리 + ETA
            if (!participant.isArrived) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatDistance(participantMarker.distanceToPin),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    participantMarker.etaMinutes?.let { eta ->
                        Text(
                            text = formatEta(eta),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 상단 라이브 공유 안내 바.
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
            .background(
                if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isActive) "📡 실시간 위치 공유 중" else "⏹ 위치 공유 중지됨",
            style = MaterialTheme.typography.labelLarge,
            color = if (isActive) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onError,
            fontWeight = FontWeight.SemiBold
        )

        if (isActive) {
            Text(
                text = "공유 끄기",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
                    .clickable(onClick = onStopSharing)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
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
