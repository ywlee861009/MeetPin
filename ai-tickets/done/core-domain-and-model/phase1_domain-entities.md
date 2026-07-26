# phase1: domain-entities

## 🎯 문제 및 목표
- 약속 생성, 대기실, 위치 추적 기능에서 공통으로 사용되는 핵심 순수 Kotlin 데이터 모델과 인터페이스 구축.

## 📝 구체적 구현 방향
1. `:core:model` 내 데이터 클래스 구현:
   - `MeetPinGroup` (groupId, title, hostId, pinLocation, scheduledAt, status, inviteCode)
   - `PinLocation` (placeName, address, latitude, longitude)
   - `Participant` (userId, nickname, profileImageUrl, inviteStatus, isArrived, arrivedAt)
   - `LocationUpdate` (groupId, userId, latitude, longitude, bearing, speed, accuracy, timestamp)
   - `GroupStatus`, `InviteStatus` 열거형 (Enum)
2. `:core:domain` 내 Repository 인터페이스 구현:
   - `MeetPinRepository`
   - `LocationRepository`

## 🔍 검증 방법
- 단위 테스트(Unit Test)를 통한 도메인 객체 데이터 불변성 검증.
