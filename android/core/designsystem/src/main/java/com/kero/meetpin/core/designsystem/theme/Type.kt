package com.kero.meetpin.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * MeetPin 타이포그래피.
 *
 * - 읽기용 텍스트는 시스템 고딕(FontFamily.Default) — 기기의 한글 고딕(Apple SD Gothic Neo /
 *   Noto Sans KR 계열)으로 렌더된다. 커스텀 폰트(Pretendard 등)를 res/font에 넣으면
 *   [MeetPinFontFamily]만 교체하면 된다.
 * - 거리·좌표·초대코드·시간 등 **정렬돼야 하는 값**은 모노스페이스 + tabular numerals.
 *
 * 스케일은 승인된 시안을 M3 슬롯에 매핑한 것이다.
 */
private val MeetPinFontFamily = FontFamily.Default
private val MonoFontFamily = FontFamily.Monospace

val MeetPinTypography = Typography(
    // Display — 히어로/큰 숫자
    displaySmall = TextStyle(
        fontFamily = MeetPinFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-1.4).sp,
    ),
    // Title — 화면 제목
    headlineSmall = TextStyle(
        fontFamily = MeetPinFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.6).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = MeetPinFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.3).sp,
    ),
    // Head — 리스트/시트 소제목
    titleMedium = TextStyle(
        fontFamily = MeetPinFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp,
    ),
    // Body
    bodyLarge = TextStyle(
        fontFamily = MeetPinFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = MeetPinFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.1.sp,
    ),
    // Labels — 버튼/필/에어브로
    labelLarge = TextStyle(
        fontFamily = MeetPinFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = MeetPinFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.2.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = MeetPinFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.4.sp,
    ),
)

/**
 * M3 Typography에 슬롯이 없는 **모노스페이스 데이터** 스타일 모음.
 * 색은 호출부에서 지정한다(테마 무관).
 */
object MeetPinTextStyles {
    /** 큰 데이터 값 (히어로 숫자 등). */
    val dataLarge = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        fontFeatureSettings = "tnum",
    )

    /** 목록 우측 거리/ETA 등. */
    val data = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontFeatureSettings = "tnum",
    )

    /** 좌표·부가 데이터 (작게). */
    val dataSmall = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontFeatureSettings = "tnum",
    )

    /** 초대코드 — 넓은 자간의 강조 모노. */
    val code = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 24.sp,
        letterSpacing = 3.5.sp,
        fontFeatureSettings = "tnum",
    )
}
