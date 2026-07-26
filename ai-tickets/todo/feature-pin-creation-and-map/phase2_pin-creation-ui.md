# phase2: pin-creation-ui

## 🎯 문제 및 목표
- 지도 상에서 클릭한 위치에 핀 마커를 표시하고, 하단 BottomSheet를 통해 약속 명칭과 약속 시각을 설정하는 MVI 화면 구현.

## 📝 구체적 구현 방향
1. `MapIntent.OnMapClicked(latLng)` 수신 시 지도 위 핀(MarkerState) 렌더링.
2. `PinCreateBottomSheet` UI 구성 (약속 제목 입력 필드, Date/Time Picker).
3. `CreatePinViewModel` (MVI) 작성:
   - `PinCreateState`: 선택한 위치, 입력된 약속 정보, 저장 중 여부
   - `PinCreateIntent`: `SelectLocation`, `UpdateTitle`, `UpdateDateTime`, `SubmitPin`

## 🔍 검증 방법
- 핀 클릭 및 정보 입력 후 제출 시 도메인 객체가 정상 생성되는지 검증.
