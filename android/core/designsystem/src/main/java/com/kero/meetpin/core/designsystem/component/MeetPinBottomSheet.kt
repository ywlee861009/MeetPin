package com.kero.meetpin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kero.meetpin.core.designsystem.theme.BottomSheetShape
import com.kero.meetpin.core.designsystem.theme.MeetPinTheme

/**
 * 지도 위에 얹히는 하단 시트 표면 — 상단만 둥글고, 그랩 핸들이 달려 있다.
 * 시트 자체는 확장 상태를 관리하지 않는다(레이아웃 컨테이너).
 */
@Composable
fun MeetPinBottomSheet(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
    showHandle: Boolean = true,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = BottomSheetShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 12.dp,
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            if (showHandle) {
                GrabHandle()
            }
            content()
        }
    }
}

@Composable
private fun GrabHandle() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(50))
                .background(MeetPinTheme.extendedColors.border),
        )
    }
}
