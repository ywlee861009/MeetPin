package com.meetpin.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.meetpin.app.permission.rememberLocationPermissionGranted
import com.meetpin.feature.lobby.InviteAcceptScreen
import com.meetpin.feature.lobby.LobbyScreen
import com.meetpin.feature.map.CreatePinScreen
import com.meetpin.feature.map.InviteShareScreen
import com.meetpin.feature.tracking.CompletionScreen
import com.meetpin.feature.tracking.LiveTrackingScreen

/**
 * MeetPin 앱의 단일 네비게이션 그래프.
 *
 * 화면 컴포저블은 `navController`를 알지 못하고 `onNavigateTo...` 람다만 받는다.
 * 이동 의도는 각 화면의 `UiEffect`에서 이미 정의되어 있고, 화면 내부에서
 * effect를 수집해 이 람다를 호출한다. 실제 `navigate()` 호출과 백스택 정책은
 * 전부 이 파일에 모인다.
 *
 * @param startDestination 시작 목적지. 기본값은 [MeetPinRoute.CREATE_PIN]이며,
 *   딥링크 cold start로 진입한 경우 초대 수락 화면으로 대체된다.
 * @param pendingInviteCode 앱 실행 중 딥링크로 도착한 초대 코드 (warm start). 없으면 `null`.
 * @param onPendingInviteCodeHandled [pendingInviteCode]를 소비했음을 알린다.
 *   호출하지 않으면 화면 회전 등 재구성마다 같은 목적지로 다시 이동한다.
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
    // 권한 상태는 그래프 전체에서 공유한다.
    // 목적지마다 따로 요청하면 화면을 옮길 때마다 시스템 다이얼로그가 다시 뜬다.
    val hasLocationPermission = rememberLocationPermissionGranted()

    // warm start 딥링크: onNewIntent로 도착한 초대 코드를 초대 수락 화면으로 넘긴다.
    LaunchedEffect(pendingInviteCode) {
        val code = pendingInviteCode ?: return@LaunchedEffect
        // 이동 전에 소비 처리한다. 같은 링크를 연속으로 탭했을 때 목적지가 쌓이지 않도록
        // launchSingleTop도 함께 지정한다.
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
                onNavigateToInviteShare = { groupId, groupTitle, inviteCode ->
                    navController.navigate(
                        MeetPinRoute.inviteShare(groupId, groupTitle, inviteCode)
                    )
                }
            )
        }

        composable(
            route = MeetPinRoute.INVITE_SHARE,
            arguments = listOf(
                navArgument(MeetPinRoute.ARG_GROUP_ID) { type = NavType.StringType },
                navArgument(MeetPinRoute.ARG_INVITE_CODE) { type = NavType.StringType },
                navArgument(MeetPinRoute.ARG_GROUP_TITLE) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { entry ->
            val groupId = entry.requireArg(MeetPinRoute.ARG_GROUP_ID)
            InviteShareScreen(
                groupTitle = entry.arguments?.getString(MeetPinRoute.ARG_GROUP_TITLE).orEmpty(),
                inviteCode = entry.requireArg(MeetPinRoute.ARG_INVITE_CODE),
                onNavigateToLobby = {
                    navController.navigate(MeetPinRoute.lobby(groupId)) {
                        // 약속 생성이 끝난 뒤 뒤로가기로 되돌아가 중복 생성하는 것을 막는다.
                        popUpTo(MeetPinRoute.CREATE_PIN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = MeetPinRoute.LOBBY,
            arguments = listOf(
                navArgument(MeetPinRoute.ARG_GROUP_ID) { type = NavType.StringType }
            )
        ) { entry ->
            LobbyScreen(
                groupId = entry.requireArg(MeetPinRoute.ARG_GROUP_ID),
                onNavigateToLiveTracking = { groupId ->
                    navController.navigate(MeetPinRoute.liveTracking(groupId)) {
                        // 전원 승낙 후 대기실로 복귀할 경로는 없다.
                        popUpTo(MeetPinRoute.LOBBY) { inclusive = true }
                        // 이동 의도가 중복 발행되어도 목적지가 쌓이지 않게 한다.
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
                onNavigateToCompletion = { groupId ->
                    navController.navigate(MeetPinRoute.completion(groupId)) {
                        // 약속이 종료된 뒤 추적 화면으로 되돌아가면 위치 공유가 다시 시작된다.
                        popUpTo(MeetPinRoute.LIVE_TRACKING) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = MeetPinRoute.COMPLETION,
            arguments = listOf(
                navArgument(MeetPinRoute.ARG_GROUP_ID) { type = NavType.StringType }
            )
        ) { entry ->
            CompletionScreen(
                groupId = entry.requireArg(MeetPinRoute.ARG_GROUP_ID),
                onNavigateToHome = {
                    navController.navigate(MeetPinRoute.CREATE_PIN) {
                        // 백스택을 비우고 핀 생성 화면에서 다시 시작한다.
                        // 딥링크로 진입한 세션에서도 시작 목적지를 기준으로 정리되도록
                        // CREATE_PIN 대신 startDestination을 pop 대상으로 쓴다.
                        popUpTo(startDestination) { inclusive = true }
                        launchSingleTop = true
                    }
                }
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
                    navController.navigate(MeetPinRoute.lobby(groupId)) {
                        // 수락이 끝난 초대장 화면으로 되돌아갈 이유가 없다.
                        popUpTo(MeetPinRoute.INVITE_ACCEPT) { inclusive = true }
                        // 호스트가 이미 대기실에 있는 상태에서 딥링크로 수락한 경우,
                        // 초대장을 pop하면 같은 대기실이 top이 된다. launchSingleTop이 없으면
                        // 대기실 항목이 두 개가 되고, 이후 트래킹의
                        // popUpTo(LOBBY)가 하나만 제거해 뒤로가기가
                        // 대기실↔트래킹 사이를 무한히 왕복한다.
                        launchSingleTop = true
                    }
                },
                onNavigateBack = {
                    // 딥링크 cold start에서는 이 화면이 시작 목적지여서 pop할 대상이 없다.
                    // 그 경우 빈 화면을 남기지 않고 앱을 종료한다.
                    if (!navController.popBackStack()) onExitApp()
                }
            )
        }
    }
}

/**
 * 라우트 패턴에 선언된 필수 인자를 꺼낸다.
 *
 * 값이 없다면 패턴과 [MeetPinRoute]의 팩토리 함수가 어긋난 프로그래밍 오류이므로 즉시 실패시킨다.
 */
private fun NavBackStackEntry.requireArg(key: String): String =
    checkNotNull(arguments?.getString(key)) {
        "라우트 인자 '$key'가 없습니다. MeetPinRoute의 패턴과 팩토리 함수를 확인하세요."
    }
