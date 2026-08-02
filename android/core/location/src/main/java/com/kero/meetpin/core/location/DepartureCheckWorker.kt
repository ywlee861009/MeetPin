package com.kero.meetpin.core.location

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.kero.meetpin.core.domain.usecase.CalculateEtaUseCase
import com.kero.meetpin.core.domain.usecase.ShouldDepartNowUseCase
import com.kero.meetpin.core.domain.usecase.TravelMode

/**
 * 출발 시점을 주기적으로 판정하는 백그라운드 Worker.
 *
 * 흐름: 현재 위치 → 목적지까지 거리 → [CalculateEtaUseCase] ETA → [ShouldDepartNowUseCase]
 * 판정 → 출발 시점이면 [DepartureNotifier]로 알림 발송 후 이 약속의 주기 작업을 스스로 취소한다
 * (중복 알림 방지).
 *
 * 기존 [LocationTrackingService]와 같이 Hilt 없이 의존성을 직접 생성한다. 순수 계산
 * 로직([CalculateEtaUseCase]/[ShouldDepartNowUseCase])은 단위 테스트로 이미 검증되어 있어,
 * Worker 자체는 배선(입력 파싱·위치 조회·발송)만 담당한다.
 *
 * 위치 권한이 없거나 마지막 위치를 얻지 못하면 이번 주기는 조용히 넘기고 다음 주기에 재시도한다.
 */
class DepartureCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val arrivalDetector = ArrivalDetector()
    private val calculateEta = CalculateEtaUseCase()
    private val shouldDepartNow = ShouldDepartNowUseCase()

    override suspend fun doWork(): Result {
        val groupId = inputData.getString(DepartureAlertScheduler.KEY_GROUP_ID)
            ?: return Result.success()
        val scheduledAt = inputData.getLong(DepartureAlertScheduler.KEY_SCHEDULED_AT, 0L)
        val targetLat = inputData.getDouble(DepartureAlertScheduler.KEY_TARGET_LAT, Double.NaN)
        val targetLng = inputData.getDouble(DepartureAlertScheduler.KEY_TARGET_LNG, Double.NaN)

        // 입력이 불완전하면(스케줄러 계약 위반) 재시도 의미가 없으므로 성공 처리로 끝낸다.
        if (scheduledAt <= 0L || targetLat.isNaN() || targetLng.isNaN()) {
            return Result.success()
        }

        val placeName = inputData.getString(DepartureAlertScheduler.KEY_PLACE_NAME).orEmpty()
        val groupTitle = inputData.getString(DepartureAlertScheduler.KEY_GROUP_TITLE).orEmpty()
        val travelMode = parseTravelMode(inputData.getString(DepartureAlertScheduler.KEY_TRAVEL_MODE))

        val current = readLastKnownLocation() ?: return Result.success()

        val distance = arrivalDetector.calculateDistance(
            current.latitude, current.longitude, targetLat, targetLng
        )
        // ETA가 null이면 이미 목적지(거리 0)라는 뜻 → 출발 알림 불필요.
        val etaMinutes = calculateEta(distanceMeters = distance, mode = travelMode)
            ?: return Result.success()

        val departNow = shouldDepartNow(
            nowMillis = System.currentTimeMillis(),
            scheduledAtMillis = scheduledAt,
            etaMinutes = etaMinutes
        )

        if (departNow) {
            DepartureNotifier(applicationContext).notifyDeparture(
                groupId = groupId,
                groupTitle = groupTitle,
                placeName = placeName,
                etaMinutes = etaMinutes
            )
            // 한 번 알렸으면 이 약속의 주기 작업을 종료해 재알림을 막는다.
            androidx.work.WorkManager.getInstance(applicationContext)
                .cancelUniqueWork(DepartureAlertScheduler.uniqueName(groupId))
        }

        return Result.success()
    }

    /** 마지막으로 알려진 위치. 권한 없음(SecurityException) 또는 캐시 없음이면 null. */
    private suspend fun readLastKnownLocation() = try {
        DefaultLocationClient(
            LocationServices.getFusedLocationProviderClient(applicationContext)
        ).getLastKnownLocation()
    } catch (_: SecurityException) {
        null
    }

    private fun parseTravelMode(raw: String?): TravelMode =
        raw?.let { name -> TravelMode.entries.firstOrNull { it.name == name } }
            ?: TravelMode.TRANSIT
}
