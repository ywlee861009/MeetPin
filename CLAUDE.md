# 🤖 CLAUDE.md - MeetPin AI 개발 가이드 & 규칙

This document serves as the central configuration and instruction guide for Claude and AI agents working on the **MeetPin** project.

---

## 📌 1. 프로젝트 개요 (Project Overview)
- **앱 이름**: MeetPin (밋핀)
- **플랫폼**: Android (Kotlin, Jetpack Compose)
- **핵심 기능**: Google Maps 기반 모임 장소 핀 설정 ➔ 딥링크 초대 ➔ 참가자 전원 승낙 조건부 실시간 위치 공유 ➔ 약속 장소 도착 시 자동 종료.
- **주요 문서**: Detailed specifications are located in [`docs/`](file:///Users/leeyoungwoo/Desktop/AndroidProjects/MeetPin/docs/README.md)
  - [`docs/01_PRD.md`](file:///Users/leeyoungwoo/Desktop/AndroidProjects/MeetPin/docs/01_PRD.md)
  - [`docs/02_ARCHITECTURE.md`](file:///Users/leeyoungwoo/Desktop/AndroidProjects/MeetPin/docs/02_ARCHITECTURE.md)
  - [`docs/03_USER_FLOW.md`](file:///Users/leeyoungwoo/Desktop/AndroidProjects/MeetPin/docs/03_USER_FLOW.md)
  - [`docs/04_DATA_MODEL_AND_API.md`](file:///Users/leeyoungwoo/Desktop/AndroidProjects/MeetPin/docs/04_DATA_MODEL_AND_API.md)

---

## 🛠️ 2. 기술 스택 & 코드 스타일 (Tech Stack & Coding Standards)
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: Modern Android Architecture (Clean Architecture: Data - Domain - UI)
- **Map & Location**: Google Maps SDK for Android (Maps Compose) + Google Play Services Fused Location Provider
- **Dependency Injection**: Hilt
- **Async & Reactive**: Kotlin Coroutines & Flow
- **Code Style**:
  - Kotlin 공식 코딩 스타일 및 Conventional Commits 준수.
  - UI 컴포넌트는 단일 책임 원칙(SRP)을 따르고 `ui/components/`에 재사용 가능하게 분리.
  - State hoisting 사용 및 ViewModel 중심의 단방향 데이터 흐름(UDF) 유지.

---

## 📋 3. AI 칸반 보드 시스템 (AI Ticket Management)
AI 에이전트 작업 관리를 위해 `ai-tickets/` 디렉토리에 파일 기반 칸반 보드를 운영합니다.

### 📂 디렉토리 구조
- [`ai-tickets/todo/`](file:///Users/leeyoungwoo/Desktop/AndroidProjects/MeetPin/ai-tickets/todo): 대기 중인 작업 티켓
- [`ai-tickets/doing/`](file:///Users/leeyoungwoo/Desktop/AndroidProjects/MeetPin/ai-tickets/doing): 현재 진행 중인 작업 티켓
- [`ai-tickets/done/`](file:///Users/leeyoungwoo/Desktop/AndroidProjects/MeetPin/ai-tickets/done): 완료된 작업 티켓

### 🏷️ 티켓 관리 규칙
1. **작업 시작 시**: `todo/`에 있는 티켓(예: `TICKET-001_setup_project.md`)을 `doing/` 폴더로 이동 후 구현 진행.
2. **작업 완료 시**: 검증(빌드/테스트) 완료 후 해당 티켓을 `done/` 폴더로 이동.
3. **티켓 작성 형식**:
   ```markdown
   # [TICKET-ID] 티켓 제목

   ## 🎯 목표
   - 작업 명세 및 요구사항

   ## 📝 세부 작업 목록
   - [ ] 세부 구현 항목 1
   - [ ] 세부 구현 항목 2

   ## 🔍 검증 기준
   - 빌드 성공 및 동작 확인 항목
   ```

---

## ⚡ 4. CLI & RTK 프록시 규칙
- **RTK Proxy**: `git`, `gradlew` 등 CLI 작업 시 토큰 절감을 위해 system hook 또는 RTK 프록시를 활용합니다.
- **Commit Rules**: 커밋 메시지는 Conventional Commits 스타일의 한글 메시지로 작성하며 승인을 받습니다.
