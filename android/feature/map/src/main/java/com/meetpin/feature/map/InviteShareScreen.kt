package com.meetpin.feature.map

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * 약속 생성 완료 후 딥링크 초대장 공유 화면.
 *
 * - 생성된 약속 요약 카드
 * - 딥링크 URL 복사 버튼
 * - 시스템 공유 팝업 (카카오톡, 문자, 기타 앱)
 */
@Composable
fun InviteShareScreen(
    groupTitle: String,
    inviteCode: String,
    onNavigateToLobby: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val inviteUrl = "https://meetpin.app/invite/$inviteCode"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 체크 아이콘
            Text(
                text = "✅",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "약속이 생성되었습니다!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 약속 요약 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = groupTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = inviteUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 공유 버튼
            Button(
                onClick = { shareInviteLink(context, groupTitle, inviteUrl) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "📤 친구에게 공유하기",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 클립보드 복사 버튼
            OutlinedButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(inviteUrl))
                    scope.launch {
                        snackbarHostState.showSnackbar("초대 링크가 복사되었습니다!")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "📋 링크 복사",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 대기실로 이동
            TextButton(onClick = onNavigateToLobby) {
                Text("대기실로 이동 →")
            }
        }
    }
}

/**
 * Android 시스템 공유 팝업을 통해 딥링크를 공유한다.
 */
private fun shareInviteLink(context: Context, title: String, url: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "MeetPin - $title")
        putExtra(
            Intent.EXTRA_TEXT,
            """
            📍 MeetPin 약속 초대!
            
            "$title" 약속에 초대합니다.
            아래 링크를 눌러 참여하세요:
            
            $url
            """.trimIndent()
        )
    }
    val shareIntent = Intent.createChooser(sendIntent, "초대장 공유")
    context.startActivity(shareIntent)
}

/**
 * TextButton - 라벨만 노출되는 텍스트 버튼 (Compose Material3 기본 제공).
 * 여기서는 "대기실로 이동 →" 용도로 사용.
 */
@Composable
private fun TextButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.TextButton(onClick = onClick) {
        content()
    }
}
