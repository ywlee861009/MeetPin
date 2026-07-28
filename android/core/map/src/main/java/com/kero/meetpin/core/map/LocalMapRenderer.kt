package com.kero.meetpin.core.map

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 현재 활성 [MapRenderer]를 제공하는 CompositionLocal.
 *
 * 앱 루트에서 `CompositionLocalProvider(LocalMapRenderer provides GoogleMapRenderer())`로 주입한다.
 * 벤더 교체는 이 provider 한 줄만 바꾸면 된다.
 */
val LocalMapRenderer = staticCompositionLocalOf<MapRenderer> {
    error(
        "MapRenderer가 제공되지 않았습니다. " +
            "앱 루트에서 CompositionLocalProvider(LocalMapRenderer provides ...)로 주입하세요."
    )
}
