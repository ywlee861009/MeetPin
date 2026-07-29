package com.kero.meetpin.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * MeetPin 원시 색 팔레트 — "모던 미니멀" 시안.
 *
 * 여기서는 값(raw hex)만 정의한다. 이 값들이 어떤 역할(primary/surface 등)로 쓰이는지는
 * [MeetPinLightColorScheme] / [MeetPinDarkColorScheme]와 [MeetPinExtendedColors]에서 매핑한다.
 *
 * 설계 원칙
 * - 강조색은 **핀 레드 하나**. "여기 / 지금 살아있는 위치"에만 쓴다.
 * - 초록(Arrived)은 강조색이 아니라 **도착 상태 전용 시맨틱 컬러**.
 * - 뉴트럴에는 지도 바탕과 어울리도록 미세한 푸른기를 넣었다.
 */

// ── Brand accent (Pin Red) ────────────────────────────────────────────────
val MeetPinRed = Color(0xFFF0454F)        // Light 강조색 / 핀 / 라이브 도트
val MeetPinRedBright = Color(0xFFFF5A63)  // Dark 강조색 (밝은 바탕 대비 보정)
val MeetPinRedWeakLight = Color(0xFFFDECED)
val MeetPinRedWeakDark = Color(0xFF3A1E20)
val MeetPinRedInkLight = Color(0xFF7A1B21) // accent-weak 위 텍스트
val MeetPinRedInkDark = Color(0xFFFFB3B8)

// ── Semantic: Arrived (도착) ──────────────────────────────────────────────
val ArrivedGreenLight = Color(0xFF12B886)
val ArrivedGreenDark = Color(0xFF21C997)
val ArrivedWeakLight = Color(0xFFE5F7F0)
val ArrivedWeakDark = Color(0xFF10241D)
val ArrivedInkLight = Color(0xFF0B6B4F)
val ArrivedInkDark = Color(0xFFAEEFD8)

// ── Neutrals (cool bias) — Light ──────────────────────────────────────────
val InkLight = Color(0xFF15171E)          // 가장 진한 제목 텍스트
val TextLight = Color(0xFF2A2D36)         // 본문
val MutedLight = Color(0xFF71767F)        // 보조 텍스트
val BorderLight = Color(0xFFE6E8EE)       // 헤어라인
val OutlineWeakLight = Color(0xFFEFF1F4)
val GroundLight = Color(0xFFFFFFFF)       // 배경
val SurfaceLight = Color(0xFFFFFFFF)      // 카드
val SurfaceContainerLight = Color(0xFFF3F4F7)

// ── Neutrals (cool bias) — Dark ───────────────────────────────────────────
val InkDark = Color(0xFFF2F4F8)
val TextDark = Color(0xFFD6D9E0)
val MutedDark = Color(0xFF8A909C)
val BorderDark = Color(0xFF272C36)
val OutlineWeakDark = Color(0xFF20242D)
val GroundDark = Color(0xFF0E1014)
val SurfaceDark = Color(0xFF15181F)
val SurfaceContainerDark = Color(0xFF1C2029)

// ── Slate (아바타·secondary) ──────────────────────────────────────────────
val SlateLight = Color(0xFF5B6472)
val SlateDark = Color(0xFF9AA0AC)

// ── Error (강조색과 구분되도록 살짝 더 진하게) ─────────────────────────────
val ErrorLight = Color(0xFFC0362F)
val ErrorWeakLight = Color(0xFFFCE8E7)
val ErrorInkLight = Color(0xFF5E1512)
val ErrorDark = Color(0xFFFF8A84)
val ErrorWeakDark = Color(0xFF3A1513)
val ErrorInkDark = Color(0xFFFFD9D6)
