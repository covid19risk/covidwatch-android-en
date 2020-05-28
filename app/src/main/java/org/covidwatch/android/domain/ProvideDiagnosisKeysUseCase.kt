package org.covidwatch.android.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.work.*
import org.covidwatch.android.domain.ProvideDiagnosisKeysUseCase.Params
import org.covidwatch.android.exposurenotification.ENStatus
import org.covidwatch.android.extension.getFinalWorkInfoByIdLiveData
import org.covidwatch.android.functional.Either
import org.covidwatch.android.work.ProvideDiagnosisKeysWork
import java.util.*
import java.util.concurrent.TimeUnit

class ProvideDiagnosisKeysUseCase(
    private val workManager: WorkManager,
    dispatchers: AppCoroutineDispatchers
) : LiveDataUseCase<UUID, Params>(dispatchers) {
    override suspend fun run(params: Params?): Either<ENStatus, UUID> {
        val recurrent = params?.recurrent ?: false
        val downloadRequest: WorkRequest
        if (recurrent) {
            downloadRequest = PeriodicWorkRequestBuilder<ProvideDiagnosisKeysWork>(
                RECURRENCE_PERIOD,
                TimeUnit.HOURS
            ).setConstraints(
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .setRequiresCharging(true)
                    .setRequiresBatteryNotLow(true)
                    .setRequiresDeviceIdle(true)
                    .setRequiresStorageNotLow(true) //TODO: Do we need this strict constraints?
                    .build()
            ).build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                downloadRequest
            )
        } else {
            downloadRequest = OneTimeWorkRequestBuilder<ProvideDiagnosisKeysWork>()
                .setConstraints(
                    Constraints.Builder().build()
                )
                .build()

            workManager.enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                downloadRequest
            )
        }

        return Either.Right(downloadRequest.id)
    }

    override suspend fun observe(params: Params?): LiveData<Either<ENStatus, UUID>> = liveData {
        run(params).apply {
            success { emitSource(workManager.getFinalWorkInfoByIdLiveData(it)) }
        }

        emit(Either.Left(ENStatus.FailedInternal))
    }

    data class Params(val recurrent: Boolean)

    companion object {
        private const val RECURRENCE_PERIOD = 24L // Hours
        const val WORK_NAME = "provide_diagnosis_keys"
    }
}