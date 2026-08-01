package com.kero.meetpin.feature.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kero.meetpin.core.designsystem.component.MeetPinButton
import com.kero.meetpin.core.designsystem.component.MeetPinCard
import com.kero.meetpin.core.designsystem.component.MeetPinGhostButton
import com.kero.meetpin.core.designsystem.component.MeetPinSecondaryButton
import com.kero.meetpin.core.model.MeetPinGroup
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 초대 수락/거절 화면.
 *
 * 딥링크로 진입한 유저에게 약속 장소/시간 카드를 보여주고
 * [승낙하기] / [거절하기] 버튼을 제공한다.
 */
@Composable
fun InviteAcceptScreen(
    inviteCode: String,
    onNavigateToLobby: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: InviteAcceptViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 초대 코드로 그룹 정보 로드
    LaunchedEffect(inviteCode) {
        viewModel.processIntent(InviteAcceptIntent.LoadInvite(inviteCode))
    }

    // Side Effect 수신
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is InviteAcceptEffect.NavigateToLobby -> onNavigateToLobby(effect.groupId)
                is InviteAcceptEffect.NavigateBack -> onNavigateBack()
                is InviteAcceptEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                }
                state.errorMessage != null && state.group == null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        // 대화면에서 텍스트가 가로로 과하게 퍼지지 않도록 폭 제한.
                        // 폰 폭(<480dp)에선 no-op이라 사이즈 클래스 게이팅이 필요 없다.
                        modifier = Modifier
                            .widthIn(max = 480.dp)
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "😢",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "초대를 불러올 수 없습니다",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        // 실패 화면이 막다른 길이 되지 않도록 재시도/돌아가기 경로를 제공한다.
                        Spacer(modifier = Modifier.height(24.dp))
                        MeetPinButton(
                            text = "다시 시도",
                            onClick = {
                                viewModel.processIntent(InviteAcceptIntent.LoadInvite(inviteCode))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        MeetPinGhostButton(
                            text = "돌아가기",
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                state.group != null -> {
                    InviteCard(
                        group = state.group!!,
                        isAccepting = state.isAccepting,
                        isDeclining = state.isDeclining,
                        onAccept = { viewModel.processIntent(InviteAcceptIntent.AcceptInvite) },
                        onDecline = { viewModel.processIntent(InviteAcceptIntent.DeclineInvite) },
                        // 대화면에서 카드가 가로로 늘어나지 않도록 폭 제한(폰에선 no-op).
                        modifier = Modifier.widthIn(max = 480.dp)
                    )
                }
            }
        }
    }
}

/**
 * 약속 정보 카드 + 승낙/거절 버튼.
 */
@Composable
private fun InviteCard(
    group: MeetPinGroup,
    isAccepting: Boolean,
    isDeclining: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("yyyy년 MM월 dd일 a hh:mm", Locale.KOREA) }

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📍",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "모임 초대장",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        MeetPinCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                // 약속 이름
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                // 장소
                Text(
                    text = "📍 ${group.pinLocation.placeName}",
                    style = MaterialTheme.typography.bodyLarge
                )

                // 일시
                Text(
                    text = "🕐 ${dateFormat.format(Date(group.scheduledAt))}",
                    style = MaterialTheme.typography.bodyLarge
                )

                // 참가자 수
                Text(
                    text = "👥 ${group.participants.size}명 참가 중",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 승낙/거절 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MeetPinSecondaryButton(
                text = "거절하기",
                onClick = onDecline,
                modifier = Modifier.weight(1f),
                enabled = !isAccepting && !isDeclining,
                loading = isDeclining
            )
            MeetPinButton(
                text = "승낙하기",
                onClick = onAccept,
                modifier = Modifier.weight(1f),
                enabled = !isAccepting && !isDeclining,
                loading = isAccepting
            )
        }
    }
}
