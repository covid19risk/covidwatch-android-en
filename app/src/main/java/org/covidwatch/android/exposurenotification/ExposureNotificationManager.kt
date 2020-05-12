package org.covidwatch.android.exposurenotification

import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import org.covidwatch.android.extension.await
import org.covidwatch.android.extension.awaitWithStatus

class ExposureNotificationManager(
    private val exposureNotification: ExposureNotificationClient
) {

    /* API */
    suspend fun start() = exposureNotification.start().awaitWithStatus()

    suspend fun temporaryExposureKeyHistory() =
        exposureNotification.temporaryExposureKeyHistory.awaitWithStatus()

    suspend fun getExposureInformation() =
        exposureNotification.exposureInformation.awaitWithStatus()

    suspend fun stop() = exposureNotification.stop().awaitWithStatus()

    suspend fun isEnabled() = exposureNotification.isEnabled.awaitWithStatus()

    suspend fun getMaxDiagnosisKeys() = exposureNotification.maxDiagnosisKeyCount.await()

    suspend fun provideDiagnosisKeys(keys: List<TemporaryExposureKey>) =
        exposureNotification.provideDiagnosisKeys(keys).await()

    suspend fun getExposureSummary() = exposureNotification.exposureSummary.await()
}