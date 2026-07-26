# phase1: google-maps-integration

## 🎯 문제 및 목표
- `:feature:map` 모듈에 Google Maps Compose 뷰를 장착하고 현재 사용자의 GPS 위치를 표시하는 기초 지도 화면 구현.

## 📝 구체적 구현 방향
1. Google Maps SDK API Key 연동 환경 마련.
2. `GoogleMap` Compose 컴포넌트 렌더링 (`MapProperties`, `MapUiSettings`).
3. 내 위치 표시 및 내 위치 바로가기 버튼 구현 (`MyLocationButton`).

## 🔍 검증 방법
- 에뮬레이터 또는 실기기에서 지도가 정상 렌더링되고 내 위치 핀이 나타나는지 확인.
