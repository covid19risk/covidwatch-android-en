package org.covidwatch.android.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.google.android.gms.nearby.Nearby
import okhttp3.OkHttpClient
import org.covidwatch.android.data.AppDatabase
import org.covidwatch.android.data.FirebaseService
import org.covidwatch.android.data.TestedRepositoryImpl
import org.covidwatch.android.data.UserFlowRepository
import org.covidwatch.android.data.diagnosiskeystoken.DiagnosisKeysTokenLocalSource
import org.covidwatch.android.data.diagnosiskeystoken.DiagnosisKeysTokenRepository
import org.covidwatch.android.data.exposureinformation.ExposureInformationLocalSource
import org.covidwatch.android.data.exposureinformation.ExposureInformationRepository
import org.covidwatch.android.data.positivediagnosis.PositiveDiagnosisRemoteSource
import org.covidwatch.android.data.positivediagnosis.PositiveDiagnosisRepository
import org.covidwatch.android.data.pref.PreferenceStorage
import org.covidwatch.android.data.pref.SharedPreferenceStorage
import org.covidwatch.android.domain.*
import org.covidwatch.android.exposurenotification.ExposureNotificationManager
import org.covidwatch.android.ui.exposurenotification.ExposureNotificationViewModel
import org.covidwatch.android.ui.exposures.ExposuresViewModel
import org.covidwatch.android.ui.home.HomeViewModel
import org.covidwatch.android.ui.onboarding.EnableExposureNotificationsViewModel
import org.covidwatch.android.ui.settings.SettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@Suppress("USELESS_CAST")
val appModule = module {
    single {
        Nearby.getExposureNotificationClient(androidApplication())
    }

    single {
        ExposureNotificationManager(
            exposureNotification = get()
        )
    }

    viewModel {
        ExposureNotificationViewModel(
            enManager = get(),
            diagnosisRepository = get(),
            provideDiagnosisKeysUseCase = get(),
            updateExposureInformationUseCase = get(),
            exposureInformationRepository = get(),
            preferenceStorage = get()
        )
    }

    viewModel {
        ExposuresViewModel(
            enManager = get(),
            updateExposureInformationUseCase = get(),
            preferenceStorage = get(),
            exposureInformationRepository = get()
        )
    }

    single { WorkManager.getInstance(androidApplication()) }

    single { AppCoroutineDispatchers() }

    single { SharedPreferenceStorage(androidApplication()) as PreferenceStorage }

    single { FirebaseService() }
    single { PositiveDiagnosisRemoteSource(firebaseService = get()) }
    single { PositiveDiagnosisRepository(remote = get()) }

    single {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java, "database.db"
        ).fallbackToDestructiveMigration().build()
    }
    single { ExposureInformationLocalSource(database = get()) }
    single { ExposureInformationRepository(local = get(), preferences = get()) }


    single {
        val appDatabase: AppDatabase = get()
        appDatabase.diagnosisKeysTokenDao()
    }
    single { DiagnosisKeysTokenLocalSource(keysTokenDao = get()) }
    single { DiagnosisKeysTokenRepository(local = get()) }

    factory {
        ProvideDiagnosisKeysUseCase(
            workManager = get(),
            dispatchers = get()
        )
    }

    factory {
        UpdateExposureStateUseCase(
            workManager = get(),
            dispatchers = get()
        )
    }

    factory {
        UpdateExposureInformationUseCase(
            exposureNotificationManager = get(),
            tokenRepository = get(),
            exposureInformationRepository = get(),
            dispatchers = get()
        )
    }

    factory {
        UserFlowRepository(
            prefs = get()
        )
    }

    factory {
        val context = androidContext()

        context.getSharedPreferences(
            "org.covidwatch.android.PREFERENCE_FILE_KEY",
            Context.MODE_PRIVATE
        )
    }

    viewModel {
        HomeViewModel(
            userFlowRepository = get(),
            testedRepository = get(),
            preferenceStorage = get()
        )
    }

    viewModel {
        SettingsViewModel(androidApplication())
    }

    single { OkHttpClient() }

    factory {
        TestedRepositoryImpl(
            preferences = get()
        ) as TestedRepository
    }

    // Onboarding start

    viewModel {
        EnableExposureNotificationsViewModel(exposureNotificationManager = get())
    }

    // Onboarding end
}