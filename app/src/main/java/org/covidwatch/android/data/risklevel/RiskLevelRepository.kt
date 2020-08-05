package org.covidwatch.android.data.risklevel

import androidx.lifecycle.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import org.covidwatch.android.data.RiskLevel
import org.covidwatch.android.data.diagnosisverification.TestType
import org.covidwatch.android.data.positivediagnosis.PositiveDiagnosisRepository
import org.covidwatch.android.data.pref.PreferenceStorage
import org.covidwatch.android.domain.AppCoroutineDispatchers
import org.covidwatch.android.extension.daysTo
import java.time.Instant

class RiskLevelRepository(
    val prefs: PreferenceStorage,
    val positiveDiagnosisRepository: PositiveDiagnosisRepository,
    val dispatchers: AppCoroutineDispatchers
) {

    val riskLevel = combine(
        prefs.observableRiskMetrics.asFlow(),
        positiveDiagnosisRepository.diagnoses().asFlow(),
        prefs.observableRegion.asFlow()
    ) { risk, diagnoses, region ->
        val recentExposureDate = risk?.mostRecentSignificantExposureDate
        when {
            region.isDisabled -> RiskLevel.DISABLED
            diagnoses.any { it.verified && TestType.CONFIRMED == it.verificationData?.testType } ->
                RiskLevel.VERIFIED_POSITIVE

            recentExposureDate != null && recentExposureDate.daysTo(Instant.now()) <= region.recentExposureDays ->
                RiskLevel.HIGH

            else -> RiskLevel.LOW
        }
    }.flowOn(dispatchers.io)

    val riskLevelNextSteps = combine(
        riskLevel,
        prefs.observableRegion.asFlow()
    ) { riskLevel, region ->
        when (riskLevel) {
            RiskLevel.VERIFIED_POSITIVE -> region.nextStepsVerifiedPositive
            RiskLevel.HIGH -> region.nextStepsSignificantExposure
            RiskLevel.LOW -> region.nextStepsNoSignificantExposure
            RiskLevel.DISABLED -> region.nextStepsDisabled ?: emptyList()
        }
    }.flowOn(dispatchers.io)
}