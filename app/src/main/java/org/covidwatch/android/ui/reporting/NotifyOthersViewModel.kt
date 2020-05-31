package org.covidwatch.android.ui.reporting

import androidx.lifecycle.map
import org.covidwatch.android.data.positivediagnosis.PositiveDiagnosisRepository
import org.covidwatch.android.domain.StartUploadDiagnosisKeysWorkUseCase
import org.covidwatch.android.ui.BaseViewModel

class NotifyOthersViewModel(
    private val startUploadDiagnosisKeysWorkUseCase: StartUploadDiagnosisKeysWorkUseCase,
    positiveDiagnosisRepository: PositiveDiagnosisRepository
) : BaseViewModel() {

    val positiveDiagnosis = positiveDiagnosisRepository.positiveDiagnosisReports().map {
        it.map { report ->
            val status = if (report.verified) TestStatus.Verified else TestStatus.NeedsVerification
            PositiveDiagnosisItem(status, report.reportDate)
        }
    }

    fun sharePositiveDiagnosis() {
        observeStatus(startUploadDiagnosisKeysWorkUseCase)
    }
}