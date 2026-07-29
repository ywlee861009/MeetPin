package com.kero.meetpin.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kero.meetpin.core.designsystem.theme.MeetPinTheme
import com.kero.meetpin.core.designsystem.theme.PillShape

/**
 * "실시간 공유 중" 배지 — 표면 위 알약, 맥동하는 라이브 도트.
 */
@Composable
fun LiveBadge(
    modifier: Modifier = Modifier,
    text: String = "실시간 공유 중",
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, PillShape)
            .border(1.dp, MeetPinTheme.extendedColors.border, PillShape)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PulsingDot(color = MaterialTheme.colorScheme.primary, size = 9.dp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MeetPinTheme.extendedColors.ink,
        )
    }
}

/**
 * 살아있음을 표현하는 도트 — 가운데 실심 원 + 바깥으로 퍼지는 링.
 *
 * 지도/위치가 "실시간"임을 알리는 단 하나의 움직임이다. 남용하지 않는다.
 */
@Composable
fun PulsingDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 10.dp,
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulse-progress",
    )

    Canvas(modifier = modifier.size(size)) {
        val center = this.center
        val core = this.size.minDimension * 0.28f
        // 퍼지는 링: 코어에서 바깥으로, 점점 옅어진다.
        val ringRadius = core + (this.size.minDimension * 0.5f - core) * progress
        drawCircle(
            color = color.copy(alpha = (1f - progress) * 0.55f),
            radius = ringRadius,
            center = center,
        )
        drawCircle(color = color, radius = core, center = center)
    }
}
