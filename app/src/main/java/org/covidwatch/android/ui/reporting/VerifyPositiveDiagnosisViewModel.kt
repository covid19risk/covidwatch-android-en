package org.covidwatch.android.ui.reporting

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.covidwatch.android.data.PositiveDiagnosisVerification
import org.covidwatch.android.data.positivediagnosis.PositiveDiagnosisRepository
import org.covidwatch.android.domain.StartUploadDiagnosisKeysWorkUseCase
import org.covidwatch.android.exposurenotification.ExposureNotificationManager
import org.covidwatch.android.extension.mutableLiveData
import org.covidwatch.android.ui.BaseViewModel

open class VerifyPositiveDiagnosisViewModel(
    private val startUploadDiagnosisKeysWorkUseCase: StartUploadDiagnosisKeysWorkUseCase,
    private val enManager: ExposureNotificationManager,
    private val positiveDiagnosisRepository: PositiveDiagnosisRepository
) : BaseViewModel() {

    private val diagnosisVerification = mutableLiveData(PositiveDiagnosisVerification())

    val readyToSubmit: LiveData<Boolean> = diagnosisVerification.map { it?.readyToSubmit ?: false }

    fun symptomsStartDate(date: Long) {
        diagnosisVerification.value = diagnosisVerification.value?.copy(symptomsStartDate = date)
    }

    fun testedDate(date: Long) {
        diagnosisVerification.value = diagnosisVerification.value?.copy(testedDate = date)
    }

    fun verificationCode(code: String) {
        diagnosisVerification.value = diagnosisVerification.value?.copy(verificationTestCode = code)
    }

    fun noSymptoms(noSymptoms: Boolean) {
        diagnosisVerification.value = diagnosisVerification.value?.copy(noSymptoms = noSymptoms)
    }

    fun sharePositiveDiagnosis() {
        viewModelScope.launch {
            enManager.isEnabled().success { enabled ->
                if (enabled) {
                    shareReport()
                } else {
                    withPermission(ExposureNotificationManager.PERMISSION_START_REQUEST_CODE) {
                        enManager.start().apply {
                            success { shareReport() }
                            failure { handleStatus(it) }
                        }
                    }
                }
            }
        }
    }

    private suspend fun shareReport() {
        withPermission(ExposureNotificationManager.PERMISSION_KEYS_REQUEST_CODE) {
            enManager.temporaryExposureKeyHistory().apply {
                success {
                    // TODO: 23.06.2020 Use proper logic for assigning transmission risk levels
                    observeStatus(
                        startUploadDiagnosisKeysWorkUseCase,
                        StartUploadDiagnosisKeysWorkUseCase.Params(it.map { 6 })
                    )
                }
                failure { handleStatus(it) }
            }
        }
    }
}