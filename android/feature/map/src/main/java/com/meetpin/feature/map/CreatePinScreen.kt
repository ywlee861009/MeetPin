package com.meetpin.feature.map

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meetpin.core.map.LocalMapRenderer
import com.meetpin.core.map.MeetPinMapUiSettings
import com.meetpin.core.model.GeoPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 핀 생성 화면 (지도 + BottomSheet).
 *
 * - 지도 터치 시 핀 마커 표시
 * - 하단 BottomSheet에서 약속 제목, 날짜/시간 입력
 * - MVI 패턴의 CreatePinViewModel과 연동
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun CreatePinScreen(
    hasLocationPermission: Boolean = false,
    onNavigateToInviteShare: (groupId: String, groupTitle: String, inviteCode: String) -> Unit =
        { _, _, _ -> },
    viewModel: CreatePinViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val mapRenderer = LocalMapRenderer.current

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // 서울 시청 기본 위치
    val defaultPosition = remember { GeoPoint(37.5666805, 126.9784147) }
    val cameraState = mapRenderer.rememberCameraState(defaultPosition, 15f)

    // Side Effect 수신
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PinCreateEffect.NavigateToInviteShare -> {
                    onNavigateToInviteShare(effect.groupId, effect.groupTitle, effect.inviteCode)
                }
                is PinCreateEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = if (state.selectedLocation != null) SheetValue.Expanded else SheetValue.PartiallyExpanded
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = bottomSheetState
    )

    // 핀 선택 시 BottomSheet 확장
    LaunchedEffect(state.selectedLocation) {
        if (state.selectedLocation != null) {
            bottomSheetState.expand()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        sheetPeekHeight = if (state.selectedLocation != null) 200.dp else 0.dp,
        sheetContent = {
            PinCreateBottomSheet(
                state = state,
                onTitleChange = { viewModel.processIntent(PinCreateIntent.UpdateTitle(it)) },
                onDateClick = { showDatePicker = true },
                onTimeClick = { showTimePicker = true },
                onSubmit = { viewModel.processIntent(PinCreateIntent.SubmitPin) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            mapRenderer.Map(
                modifier = Modifier.fillMaxSize(),
                cameraState = cameraState,
                myLocationEnabled = hasLocationPermission,
                uiSettings = MeetPinMapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = hasLocationPermission
                ),
                onMapClick = { point ->
                    viewModel.processIntent(PinCreateIntent.SelectLocation(point))
                }
            ) {
                // 선택된 위치에 핀 마커 표시
                state.selectedLocation?.let { location ->
                    Marker(
                        position = location,
                        title = state.title.ifBlank { "약속 장소" },
                        snippet = "여기서 만나요!"
                    )
                }
            }
        }
    }

    // Date Picker 다이얼로그
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.processIntent(PinCreateIntent.UpdateDate(millis))
                    }
                    showDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker 다이얼로그
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                val millis = (timePickerState.hour * 60 + timePickerState.minute) * 60 * 1000L
                viewModel.processIntent(PinCreateIntent.UpdateTime(millis))
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

/**
 * 핀 생성 하단 BottomSheet UI.
 */
@Composable
private fun PinCreateBottomSheet(
    state: PinCreateState,
    onTitleChange: (String) -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA) }
    val timeFormat = remember { SimpleDateFormat("a hh:mm", Locale.KOREA) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "약속 만들기",
            style = MaterialTheme.typography.titleLarge
        )

        // 약속 제목 입력
        OutlinedTextField(
            value = state.title,
            onValueChange = onTitleChange,
            label = { Text("약속 이름") },
            placeholder = { Text("예: 홍대 저녁 모임") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // 날짜 선택
        OutlinedTextField(
            value = state.scheduledDate?.let { dateFormat.format(Date(it)) } ?: "",
            onValueChange = {},
            label = { Text("약속 날짜") },
            placeholder = { Text("날짜를 선택하세요") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = true,
            interactionSource = rememberClickInteractionSource(onClick = onDateClick)
        )

        // 시간 선택
        OutlinedTextField(
            value = state.scheduledTime?.let {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, (it / (60 * 60 * 1000)).toInt())
                    set(Calendar.MINUTE, ((it / (60 * 1000)) % 60).toInt())
                }
                timeFormat.format(cal.time)
            } ?: "",
            onValueChange = {},
            label = { Text("약속 시간") },
            placeholder = { Text("시간을 선택하세요") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = true,
            interactionSource = rememberClickInteractionSource(onClick = onTimeClick)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 제출 버튼
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.isFormValid && !state.isSubmitting
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    strokeWidth = 2.dp
                )
            }
            Text("약속 만들기")
        }
    }
}
