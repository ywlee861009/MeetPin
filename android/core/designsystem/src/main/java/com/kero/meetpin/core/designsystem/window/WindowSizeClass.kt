package com.kero.meetpin.core.designsystem.window

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * 현재 창의 [WindowSizeClass]를 하위 컴포저블에 공급하는 CompositionLocal.
 *
 * 앱 진입점(`MainActivity`)에서 `calculateWindowSizeClass(activity)` 결과를 provide 하고,
 * feature 화면들은 이 값을 읽어 폰(Compact) / 태블릿·폴더블(Expanded) 레이아웃을 분기한다.
 *
 * feature 모듈이 `:app` 의존 없이 소비할 수 있도록 `:core:designsystem`에 둔다.
 *
 * 기본값은 Compact(폰) 크기 — provider가 없는 `@Preview`·테스트 환경에서도
 * 크래시 없이 폰 레이아웃으로 렌더되도록 한다.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
val LocalWindowSizeClass = staticCompositionLocalOf {
    WindowSizeClass.calculateFromSize(DpSize(360.dp, 640.dp))
}

/** 좌우 분할 등 '태블릿스러운' 레이아웃을 적용할 넓은 화면인지 여부(width Expanded). */
val WindowSizeClass.isExpandedWidth: Boolean
    get() = widthSizeClass == WindowWidthSizeClass.Expanded

/** 단일 컬럼 폰 레이아웃을 적용할 좁은 화면인지 여부(width Compact). */
val WindowSizeClass.isCompactWidth: Boolean
    get() = widthSizeClass == WindowWidthSizeClass.Compact
