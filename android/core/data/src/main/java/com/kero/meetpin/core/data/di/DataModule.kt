package com.kero.meetpin.core.data.di

import com.kero.meetpin.core.data.repository.FakeMeetPinRepository
import com.kero.meetpin.core.domain.repository.MeetPinRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Data 계층 Hilt 바인딩.
 *
 * 실제 서버 연동 시에는 [FakeMeetPinRepository] 대신 `:core:network` 기반 구현체를
 * 여기서 바인딩하도록 교체한다. ViewModel은 인터페이스에만 의존하므로 수정이 필요 없다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindMeetPinRepository(impl: FakeMeetPinRepository): MeetPinRepository
}
