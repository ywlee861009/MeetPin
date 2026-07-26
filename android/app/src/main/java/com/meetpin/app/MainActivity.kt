package com.meetpin.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meetpin.app.navigation.MeetPinNavHost
import com.meetpin.app.navigation.MeetPinRoute
import com.meetpin.app.ui.theme.MeetPinTheme
import com.meetpin.feature.lobby.DeepLinkHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * MeetPin의 단일 Activity 진입점.
 *
 * 화면 구성은 전부 [MeetPinNavHost]가 담당한다. 각 화면이 자체 `Scaffold`를 갖고 있으므로
 * 여기서 `Scaffold`로 한 번 더 감싸지 않는다 (system bar inset이 이중 적용된다).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * 앱이 실행 중일 때 도착한 딥링크 초대 코드.
     *
     * cold start는 시작 목적지 자체를 바꿔 처리하므로, 이 흐름은 `onNewIntent`로 들어오는
     * warm start 재진입만 담당한다.
     */
    private val pendingInviteCode = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // cold start 딥링크는 navigate가 아니라 시작 목적지 교체로 처리한다.
        // createPin을 먼저 띄우고 이동하면 한 프레임 동안 핀 생성 화면이 깜빡이고,
        // 초대받은 사용자가 뒤로가기를 눌렀을 때 남의 약속 생성 화면으로 떨어진다.
        val startInviteCode = DeepLinkHandler.extractInviteCode(intent)

        setContent {
            MeetPinTheme {
                val inviteCode by pendingInviteCode.collectAsStateWithLifecycle()
                MeetPinNavHost(
                    startDestination = startInviteCode
                        ?.let(MeetPinRoute::inviteAccept)
                        ?: MeetPinRoute.CREATE_PIN,
                    pendingInviteCode = inviteCode,
                    onPendingInviteCodeHandled = { pendingInviteCode.value = null },
                    onExitApp = { finish() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // setIntent를 호출하지 않으면 이후 getIntent()가 최초 intent를 계속 반환한다.
        setIntent(intent)
        DeepLinkHandler.extractInviteCode(intent)?.let { pendingInviteCode.value = it }
    }
}
