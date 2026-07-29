package com.kero.meetpin.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 지도 핀(물방울) 마커. 브랜드 강조색으로 채우고 가운데 구멍을 뚫는다.
 *
 * 약속 장소 핀([color] = ink)과 라이브 위치 핀([color] = primary)에 모두 쓴다.
 * 앵커는 물방울 끝(하단 중앙)이다.
 */
@Composable
fun PinMarker(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    holeColor: Color = MaterialTheme.colorScheme.surface,
) {
    Canvas(modifier = modifier.size(width = size, height = size * 1.25f)) {
        val w = this.size.width
        val r = w / 2f
        val cx = w / 2f
        val circleCenter = Offset(cx, r)

        // 원(머리)
        drawCircle(color = color, radius = r, center = circleCenter)

        // 뾰족한 끝(꼬리) — 원과 겹치게 그려 물방울 실루엣을 만든다.
        val tail = Path().apply {
            moveTo(cx, this@Canvas.size.height)
            lineTo(cx - r * 0.72f, r * 1.15f)
            lineTo(cx + r * 0.72f, r * 1.15f)
            close()
        }
        drawPath(path = tail, color = color)

        // 가운데 구멍
        drawCircle(color = holeColor, radius = r * 0.42f, center = circleCenter)
    }
}
