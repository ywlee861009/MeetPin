package com.kero.meetpin.core.location.di

import android.content.Context
import androidx.work.WorkManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kero.meetpin.core.domain.repository.LocationRepository
import com.kero.meetpin.core.location.ArrivalDetector
import com.kero.meetpin.core.location.DefaultLocationClient
import com.kero.meetpin.core.location.DefaultLocationRepository
import com.kero.meetpin.core.location.LocationClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 위치 계층 Hilt 바인딩.
 *
 * `DefaultLocationClient` / `DefaultLocationRepository` / `ArrivalDetector`는
 * Dagger 어노테이션 없는 순수 클래스로 유지하고(단위 테스트에서 직접 생성 가능하도록),
 * 생성 책임만 이 모듈이 담당한다.
 */
@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @Provides
    @Singleton
    fun provideLocationClient(
        fusedClient: FusedLocationProviderClient
    ): LocationClient = DefaultLocationClient(fusedClient)

    @Provides
    @Singleton
    fun provideLocationRepository(
        locationClient: LocationClient
    ): LocationRepository = DefaultLocationRepository(locationClient)

    @Provides
    @Singleton
    fun provideArrivalDetector(): ArrivalDetector = ArrivalDetector()

    /**
     * 출발 알림 스케줄러([DepartureAlertScheduler])가 주입받는 WorkManager.
     * 앱 기본 초기화(androidx.startup)로 생성된 싱글턴 인스턴스를 그대로 공급한다.
     */
    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager = WorkManager.getInstance(context)
}
