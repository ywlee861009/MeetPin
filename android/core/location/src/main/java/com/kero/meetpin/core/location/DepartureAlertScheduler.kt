package com.kero.meetpin.core.location

import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kero.meetpin.core.domain.usecase.TravelMode
import com.kero.meetpin.core.model.MeetPinGroup
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * 출발 알림 주기 작업(WorkManager) 스케줄러.
 *
 * 약속 하나당 고유 이름([uniqueName])의 주기 작업을 등록한다. WorkManager 주기 작업의 최소
 * 간격은 15분이므로 [CHECK_INTERVAL_MINUTES]도 15분이다. 작업 본문은 [DepartureCheckWorker].
 *
 * 트래킹이 시작되면 [schedule], 종료되면 [cancel]을 호출한다. 알림이 한 번 발송되면
 * Worker가 스스로 작업을 취소하므로(중복 알림 방지), 정상 흐름에서 [cancel]은 사용자가
 * 위치 공유를 끄는 등 조기 종료 시에 쓰인다.
 */
class DepartureAlertScheduler @Inject constructor(
    private val workManager: WorkManager
) {

    /**
     * 약속 [group]에 대한 출발 알림 주기 체크를 등록한다.
     * 이미 같은 약속의 작업이 있으면 최신 정보(약속 시각·위치)로 갱신한다([ExistingPeriodicWorkPolicy.UPDATE]).
     *
     * @param travelMode ETA 계산에 쓸 이동 수단. 기본값 대중교통.
     */
    fun schedule(group: MeetPinGroup, travelMode: TravelMode = TravelMode.TRANSIT) {
        val request = PeriodicWorkRequestBuilder<DepartureCheckWorker>(
            CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES
        )
            .setInputData(buildInputData(group, travelMode))
            .build()

        workManager.enqueueUniquePeriodicWork(
            uniqueName(group.id),
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /**
     * [DEBUG/QA 전용] 주기(최소 15분)를 기다리지 않고 출발 판정을 **즉시 1회** 실행한다.
     * 주기 작업과 동일한 [DepartureCheckWorker]·입력 데이터를 쓰므로, 위치→거리→ETA→판정→알림
     * →딥링크의 실제 경로를 그대로 검증할 수 있다. 데모 그룹은 약속 시각이 "지금"이라 위치만
     * 잡히면 출발 알림이 발송된다.
     */
    fun checkNow(group: MeetPinGroup, travelMode: TravelMode = TravelMode.TRANSIT) {
        val request = OneTimeWorkRequestBuilder<DepartureCheckWorker>()
            .setInputData(buildInputData(group, travelMode))
            .build()
        workManager.enqueue(request)
    }

    /** 약속 [groupId]의 출발 알림 주기 작업을 취소한다. */
    fun cancel(groupId: String) {
        workManager.cancelUniqueWork(uniqueName(groupId))
    }

    private fun buildInputData(group: MeetPinGroup, travelMode: TravelMode): Data =
        workDataOf(
            KEY_GROUP_ID to group.id,
            KEY_GROUP_TITLE to group.title,
            KEY_SCHEDULED_AT to group.scheduledAt,
            KEY_TARGET_LAT to group.pinLocation.latitude,
            KEY_TARGET_LNG to group.pinLocation.longitude,
            KEY_PLACE_NAME to group.pinLocation.placeName,
            KEY_TRAVEL_MODE to travelMode.name
        )

    companion object {
        /** WorkManager 주기 작업 최소 간격(분). */
        const val CHECK_INTERVAL_MINUTES = 15L

        const val KEY_GROUP_ID = "groupId"
        const val KEY_GROUP_TITLE = "groupTitle"
        const val KEY_SCHEDULED_AT = "scheduledAt"
        const val KEY_TARGET_LAT = "targetLat"
        const val KEY_TARGET_LNG = "targetLng"
        const val KEY_PLACE_NAME = "placeName"
        const val KEY_TRAVEL_MODE = "travelMode"

        private const val UNIQUE_NAME_PREFIX = "departure_alert_"

        /** 약속별 고유 작업 이름. Worker가 스스로 취소할 때도 같은 규칙을 쓴다. */
        fun uniqueName(groupId: String): String = "$UNIQUE_NAME_PREFIX$groupId"
    }
}
