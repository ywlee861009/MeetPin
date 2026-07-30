package com.kero.meetpin.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kero.meetpin.app.navigation.MeetPinNavHost
import com.kero.meetpin.app.navigation.MeetPinRoute
import com.kero.meetpin.core.designsystem.theme.MeetPinTheme
import com.kero.meetpin.core.designsystem.window.LocalWindowSizeClass
import com.kero.meetpin.core.map.LocalMapRenderer
import com.kero.meetpin.core.map.google.GoogleMapRenderer
import com.kero.meetpin.feature.lobby.DeepLinkHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * MeetPin의 단일 Activity 진입점.
 *
 * 화면 구성은 전부 [MeetPinNavHost]가 담당한다. 각 화면이 자체 `Scaffold`를 갖고 있으므로
 * 여기서 `Scaffold`로 한 번 더 감싸지 않는다 (system bar inset이 이중 적용된다).
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
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
                // 지도 벤더 주입 지점. 카카오/네이버로 교체 시 이 한 줄만 바꾼다.
                val mapRenderer = remember { GoogleMapRenderer() }
                // 창 크기 분류(Compact/Medium/Expanded)를 계산해 전역 공급한다.
                // feature 화면들은 LocalWindowSizeClass로 읽어 폰/태블릿 레이아웃을 분기한다.
                val windowSizeClass = calculateWindowSizeClass(this)
                // phase1 검증: 폰(Compact) vs 태블릿(Expanded) 인식 여부를 로그로 확인.
                LaunchedEffect(windowSizeClass) {
                    Log.d(
                        "WindowSizeClass",
                        "width=${windowSizeClass.widthSizeClass}, height=${windowSizeClass.heightSizeClass}"
                    )
                }
                CompositionLocalProvider(
                    LocalMapRenderer provides mapRenderer,
                    LocalWindowSizeClass provides windowSizeClass,
                ) {
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
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // setIntent를 호출하지 않으면 이후 getIntent()가 최초 intent를 계속 반환한다.
        setIntent(intent)
        DeepLinkHandler.extractInviteCode(intent)?.let { pendingInviteCode.value = it }
    }
}
