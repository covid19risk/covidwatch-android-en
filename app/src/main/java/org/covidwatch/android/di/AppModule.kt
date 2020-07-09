package org.covidwatch.android.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.google.android.gms.nearby.Nearby
import com.google.common.io.BaseEncoding
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.logging.HttpLoggingInterceptor.Level.NONE
import org.covidwatch.android.BuildConfig
import org.covidwatch.android.R
import org.covidwatch.android.data.*
import org.covidwatch.android.data.countrycode.CountryCodeRepository
import org.covidwatch.android.data.diagnosiskeystoken.DiagnosisKeysTokenLocalSource
import org.covidwatch.android.data.diagnosiskeystoken.DiagnosisKeysTokenRepository
import org.covidwatch.android.data.diagnosisverification.DiagnosisVerificationRemoteSource
import org.covidwatch.android.data.diagnosisverification.DiagnosisVerificationRepository
import org.covidwatch.android.data.exposureinformation.ExposureInformationLocalSource
import org.covidwatch.android.data.exposureinformation.ExposureInformationRepository
import org.covidwatch.android.data.positivediagnosis.PositiveDiagnosisLocalSource
import org.covidwatch.android.data.positivediagnosis.PositiveDiagnosisRemoteSource
import org.covidwatch.android.data.positivediagnosis.PositiveDiagnosisRepository
import org.covidwatch.android.data.pref.PreferenceStorage
import org.covidwatch.android.data.pref.SharedPreferenceStorage
import org.covidwatch.android.domain.*
import org.covidwatch.android.exposurenotification.ExposureNotificationManager
import org.covidwatch.android.ui.Notifications
import org.covidwatch.android.ui.exposurenotification.ExposureNotificationViewModel
import org.covidwatch.android.ui.exposures.ExposuresViewModel
import org.covidwatch.android.ui.home.HomeViewModel
import org.covidwatch.android.ui.menu.MenuViewModel
import org.covidwatch.android.ui.onboarding.EnableExposureNotificationsViewModel
import org.covidwatch.android.ui.reporting.VerifyPositiveDiagnosisViewModel
import org.covidwatch.android.ui.settings.SettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.security.SecureRandom

val appModule = module {
    single {
        Nearby.getExposureNotificationClient(androidApplication())
    }

    single<EnConverter> { ArizonaEnConverter() }

    single { Notifications(context = androidApplication()) }

    single {
        ExposureNotificationManager(
            exposureNotification = get()
        )
    }

    single {
        DiagnosisVerificationManager(
            verificationRepository = get()
        )
    }

    viewModel {
        ExposureNotificationViewModel(
            enManager = get(),
            uploadDiagnosisKeysUseCase = get(),
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

    single<PreferenceStorage> { SharedPreferenceStorage(androidApplication()) }

    single {
        PositiveDiagnosisRemoteSource(
            httpClient = get(),
            keysDir = androidContext().filesDir.absolutePath
        )
    }
    single {
        val appDatabase: AppDatabase = get()
        appDatabase.positiveDiagnosisReportDao()
    }
    single { PositiveDiagnosisLocalSource(reportDao = get()) }
    single {
        PositiveDiagnosisRepository(
            remote = get(),
            local = get(),
            countryCodeRepository = get(),
            uriManager = get(),
            dispatchers = get()
        )
    }

    single {
        DiagnosisVerificationRemoteSource(
            apiKey = androidContext().getString(R.string.verification_api_key),
            verificationServerEndpoint = BuildConfig.SERVER_VERIFICATION_ENDPOINT,
            gson = Gson(),
            httpClient = get()
        )
    }
    single {
        DiagnosisVerificationRepository(
            remote = get(),
            dispatchers = get()
        )
    }

    single {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java, "database.db"
        ).fallbackToDestructiveMigration().build()
    }
    single {
        val appDatabase: AppDatabase = get()
        appDatabase.exposureInformationDao()
    }
    single { ExposureInformationLocalSource(dao = get()) }
    single {
        ExposureInformationRepository(
            local = get(),
            dispatchers = get()
        )
    }


    single {
        val appDatabase: AppDatabase = get()
        appDatabase.diagnosisKeysTokenDao()
    }
    single { DiagnosisKeysTokenLocalSource(keysTokenDao = get()) }
    single {
        DiagnosisKeysTokenRepository(
            local = get(),
            dispatchers = get()
        )
    }

    single {
        val appDatabase: AppDatabase = get()
        appDatabase.countryCodeDao()
    }
    single {
        CountryCodeRepository(
            local = get(),
            dispatchers = get()
        )
    }

    single {
        UriManager(
            serverUploadEndpoint = BuildConfig.SERVER_UPLOAD_ENDPOINT,
            serverDownloadEndpoint = BuildConfig.SERVER_DOWNLOAD_ENDPOINT,
            httpClient = get()
        )
    }

    factory {
        ProvideDiagnosisKeysUseCase(
            workManager = get(),
            dispatchers = get()
        )
    }

    factory {
        UploadDiagnosisKeysUseCase(
            enManager = get(),
            diagnosisRepository = get(),
            countryCodeRepository = get(),
            verificationManager = get(),
            uriManager = get(),
            appPackageName = androidContext().packageName,
            random = SecureRandom(),
            encoding = BaseEncoding.base64(),
            dispatchers = get()
        )
    }

    factory {
        StartUploadDiagnosisKeysWorkUseCase(
            workManager = get(),
            dispatchers = get(),
            positiveDiagnosisRepository = get()
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
            enManager = get(),
            tokenRepository = get(),
            exposureInformationRepository = get(),
            enConverter = get(),
            dispatchers = get()
        )
    }

    single {
        UserFlowRepository(
            prefs = get()
        )
    }

    single {
        val context = androidContext()

        context.getSharedPreferences(
            "org.covidwatch.android.PREFERENCE_FILE_KEY",
            Context.MODE_PRIVATE
        )
    }

    viewModel {
        HomeViewModel(
            enManager = get(),
            userFlowRepository = get(),
            preferenceStorage = get()
        )
    }

    viewModel {
        SettingsViewModel(androidApplication())
    }

    viewModel {
        MenuViewModel(exposureInformationRepository = get())
    }

    viewModel {
        VerifyPositiveDiagnosisViewModel(
            startUploadDiagnosisKeysWorkUseCase = get(),
            enManager = get()
        )
    }

    single {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(if (BuildConfig.DEBUG) BODY else NONE)

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(ConnectivityInterceptor(androidApplication()))
            .build()
    }

    single<TestedRepository> {
        TestedRepositoryImpl(
            preferences = get()
        )
    }

    // Onboarding start

    viewModel {
        EnableExposureNotificationsViewModel(enManager = get(), userFlowRepository = get())
    }

    // Onboarding end
}