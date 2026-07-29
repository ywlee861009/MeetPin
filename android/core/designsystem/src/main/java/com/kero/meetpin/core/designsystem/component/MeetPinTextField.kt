package com.kero.meetpin.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.kero.meetpin.core.designsystem.theme.MeetPinTheme

/**
 * MeetPin 기본 텍스트 필드 — 토큰 기반 외곽선 인풋.
 *
 * 폼·채팅 입력에 공통으로 쓴다. 색/모양은 DS 토큰으로 고정하고, 포커스 시 강조색 외곽선을 준다.
 * 기본 모양은 인풋 토큰([MaterialTheme.shapes] small).
 *
 * @param trailingIcon 전송 버튼 등 우측 슬롯(선택).
 */
@Composable
fun MeetPinTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = false,
    shape: Shape = MaterialTheme.shapes.small,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        singleLine = singleLine,
        shape = shape,
        textStyle = MaterialTheme.typography.bodyMedium,
        placeholder = placeholder?.let {
            { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
        },
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MeetPinTheme.extendedColors.border,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
    )
}
