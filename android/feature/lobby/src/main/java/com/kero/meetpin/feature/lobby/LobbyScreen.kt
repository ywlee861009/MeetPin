package com.kero.meetpin.feature.lobby

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kero.meetpin.core.model.InviteStatus
import com.kero.meetpin.core.model.Participant
import kotlinx.coroutines.flow.collectLatest

/**
 * 대기실(Lobby) 화면.
 *
 * - 약속 정보 렌더링 카드
 * - 참가자 프로필 리스트 및 상태 칩 (🟢 Accepted, 🟡 Pending, 🔴 Declined)
 * - 전원 승낙 진행도 바
 * - 전원 승낙 시 자동으로 라이브 트래킹 화면으로 전환
 */
@Composable
fun LobbyScreen(
    groupId: String,
    onNavigateToLiveTracking: (String) -> Unit = {},
    viewModel: LobbyViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(groupId) {
        viewModel.processIntent(LobbyIntent.ObserveGroup(groupId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LobbyEffect.NavigateToLiveTracking -> onNavigateToLiveTracking(effect.groupId)
                is LobbyEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            ) {
                // 헤더
                Text(
                    text = "⏳ 대기실",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = state.groupTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 진행도 바
                ProgressCard(
                    acceptedCount = state.acceptedCount,
                    totalCount = state.totalCount,
                    progressRatio = state.progressRatio
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 참가자 리스트
                Text(
                    text = "참가자 현황",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.participants, key = { it.userId }) { participant ->
                        ParticipantCard(participant = participant)
                    }
                }
            }
        }
    }
}

/**
 * 전원 승낙 진행도 카드.
 */
@Composable
private fun ProgressCard(
    acceptedCount: Int,
    totalCount: Int,
    progressRatio: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "승낙 현황",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$acceptedCount / $totalCount 명",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progressRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
            )

            if (progressRatio >= 1f) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "🎉 전원 승낙 완료! 곧 위치 공유가 시작됩니다...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * 참가자 상태 카드.
 */
@Composable
private fun ParticipantCard(
    participant: Participant,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = participant.nickname.take(1),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 이름
            Text(
                text = participant.nickname,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            // 상태 칩
            StatusChip(status = participant.inviteStatus)
        }
    }
}

/**
 * 초대 상태 표시 칩.
 * 🟢 Accepted, 🟡 Pending, 🔴 Declined
 */
@Composable
private fun StatusChip(
    status: InviteStatus,
    modifier: Modifier = Modifier
) {
    val (emoji, label, bgColor) = when (status) {
        InviteStatus.ACCEPTED -> Triple("🟢", "승낙", Color(0xFF4CAF50).copy(alpha = 0.15f))
        InviteStatus.PENDING -> Triple("🟡", "대기", Color(0xFFFFC107).copy(alpha = 0.15f))
        InviteStatus.DECLINED -> Triple("🔴", "거절", Color(0xFFF44336).copy(alpha = 0.15f))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$emoji $label",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
