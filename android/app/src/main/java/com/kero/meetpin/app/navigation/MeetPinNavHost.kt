package com.kero.meetpin.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kero.meetpin.app.BuildConfig
import com.kero.meetpin.app.permission.rememberLocationPermissionGranted
import com.kero.meetpin.feature.lobby.InviteAcceptScreen
import com.kero.meetpin.feature.map.CreatePinScreen
import com.kero.meetpin.feature.tracking.LiveTrackingScreen

/**
 * MeetPin 앱의 단일 네비게이션 그래프.
 *
 * 즉시 공유 플로우: 핀 생성 → 실시간 지도, 초대 수락 → 실시간 지도.
 * 대기실·초대공유·완료 화면은 없다.
 *
 * @param startDestination 시작 목적지. 기본값은 [MeetPinRoute.CREATE_PIN]이며,
 *   딥링크 cold start로 진입한 경우 초대 수락 화면으로 대체된다.
 * @param pendingInviteCode 앱 실행 중 딥링크로 도착한 초대 코드 (warm start). 없으면 `null`.
 * @param onPendingInviteCodeHandled [pendingInviteCode]를 소비했음을 알린다.
 * @param onExitApp 백스택이 비어 뒤로가기를 처리할 수 없을 때 앱을 종료한다.
 */
@Composable
fun MeetPinNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = MeetPinRoute.CREATE_PIN,
    pendingInviteCode: String? = null,
    onPendingInviteCodeHandled: () -> Unit = {},
    onExitApp: () -> Unit = {}
) {
    val hasLocationPermission = rememberLocationPermissionGranted()

    // warm start 딥링크: onNewIntent로 도착한 초대 코드를 초대 수락 화면으로 넘긴다.
    LaunchedEffect(pendingInviteCode) {
        val code = pendingInviteCode ?: return@LaunchedEffect
        onPendingInviteCodeHandled()
        navController.navigate(MeetPinRoute.inviteAccept(code)) {
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = MeetPinRoute.CREATE_PIN) {
            CreatePinScreen(
                hasLocationPermission = hasLocationPermission,
                onNavigateToLiveTracking = { groupId ->
                    navController.navigate(MeetPinRoute.liveTracking(groupId)) {
                        // 생성 후 뒤로가기로 돌아가 중복 생성하는 것을 막는다.
                        popUpTo(MeetPinRoute.CREATE_PIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = MeetPinRoute.LIVE_TRACKING,
            arguments = listOf(
                navArgument(MeetPinRoute.ARG_GROUP_ID) { type = NavType.StringType }
            )
        ) { entry ->
            LiveTrackingScreen(
                groupId = entry.requireArg(MeetPinRoute.ARG_GROUP_ID),
                hasLocationPermission = hasLocationPermission,
                // 단일 기기 시연을 위한 '상대 수락 시뮬' 버튼은 debug 빌드에서만 노출한다.
                showDebugTools = BuildConfig.DEBUG
            )
        }

        composable(
            route = MeetPinRoute.INVITE_ACCEPT,
            arguments = listOf(
                navArgument(MeetPinRoute.ARG_INVITE_CODE) { type = NavType.StringType }
            )
        ) { entry ->
            InviteAcceptScreen(
                inviteCode = entry.requireArg(MeetPinRoute.ARG_INVITE_CODE),
                onNavigateToLobby = { groupId ->
                    // 대기실 없이 곧바로 실시간 지도로 진입한다.
                    navController.navigate(MeetPinRoute.liveTracking(groupId)) {
                        popUpTo(MeetPinRoute.INVITE_ACCEPT) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateBack = {
                    // 딥링크 cold start에서는 이 화면이 시작 목적지여서 pop할 대상이 없다.
                    if (!navController.popBackStack()) onExitApp()
                }
            )
        }
    }
}

/**
 * 라우트 패턴에 선언된 필수 인자를 꺼낸다.
 */
private fun NavBackStackEntry.requireArg(key: String): String =
    checkNotNull(arguments?.getString(key)) {
        "라우트 인자 '$key'가 없습니다. MeetPinRoute의 패턴과 팩토리 함수를 확인하세요."
    }
