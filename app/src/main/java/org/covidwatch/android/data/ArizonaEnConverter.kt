package org.covidwatch.android.data

import com.google.android.gms.nearby.exposurenotification.ExposureInformation
import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import java.util.*
import kotlin.math.exp
import kotlin.math.pow

class ArizonaEnConverter : EnConverter {

    /**
     * According to preliminary dose estimates, the high attenuation distance has a dose 7 times
     * higher than the medium attenuation distance.
     *
     * The mean dose for the low attenuation distance is 0.2 the mean dose of the medium attenuation distance.
     * -AW 6/7/2020
     */
    private val attenuationDurationWeights = doubleArrayOf(
        2.0182978, // High attenuation: D < 0.5m
        1.1507629, // Medium attenuation: 0.5m < D < 2m
        0.6651614 // Low attenuation: 2m < D
    )

    private val doseResponseLambda = 1.71E-05

    /**
     * High range shedding ~1010 copies/m3
     * Medium range shedding ~107 copies/m3 (based on estimates of high asymptomatic shedders)
     * Low  range shedding ~104
     *
     * Then assuming between 0.01% and 1% infectivity.
     * Will use copies/m3 since infectivity assumed to apply the same to these concentrations.
     * In the future, we could relate these concentraitons to cycle threshold values of studies
     * to gain more insights into how fractions of infectivity may vary by concentration
     *
     * Transmission risk values increase on a log10 scale, with a 0 transmission level
     * translating to a 0 transmission risk value. These are then multiplied by the time-weighted
     * sum of attenuation. The log10 of this product yields the risk score.
     * Risk scores then translate to risk levels, with assignments described in D2:10 through E2:10.
     * Examples are below.
     */
    private val transmissionRiskValuesForLevels = doubleArrayOf(
        0.00E+00, // Level 0
        1.00E+01, // Level 1
        10.0.pow(1 + 2 / 6.0), // Level 2
        10.0.pow(1 + 3 / 6.0), // Level 3
        10.0.pow(1 + 4 / 6.0), // Level 4
        10.0.pow(1 + 5 / 6.0), // Level 5
        10.0.pow(1 + 6 / 6.0), // Level 6
        10.0.pow(1 + 7 / 6.0)  // Level 7 (unused)
    )

    private fun computeAttenuationDurationRiskScore(attenuationDurations: IntArray): Double {
        if (attenuationDurations.size != attenuationDurationWeights.size) return 0.0

        return attenuationDurations[0].toDouble() * attenuationDurationWeights[0] +
                attenuationDurations[1].toDouble() * attenuationDurationWeights[1] +
                attenuationDurations[2].toDouble() * attenuationDurationWeights[2]
    }

    private fun computeRiskScore(
        attenuationDurations: IntArray,
        transmissionRiskLevel: Int
    ): RiskScore {
        val transmissionRiskValue = transmissionRiskValuesForLevels[transmissionRiskLevel]
        val attenuationDurationRiskScore = computeAttenuationDurationRiskScore(attenuationDurations)
        val score =
            (1 - exp(-doseResponseLambda * transmissionRiskValue * attenuationDurationRiskScore)) * 100
        return when {
            score.within(Double.MIN_VALUE, 1.0) -> 0
            score.within(1.0, 1.5) -> 1
            score.within(1.5, 2.0) -> 2
            score.within(2.0, 2.5) -> 3
            score.within(2.5, 3.0) -> 4
            score.within(3.0, 3.5) -> 5
            score.within(3.5, 4.0) -> 6
            score.within(4.0, 4.5) -> 7
            else -> 8
        }
    }

    private fun Double.within(min: Double, max: Double) = this >= min && this < max

    override fun covidExposureSummary(exposureSummary: ExposureSummary) =
        with(exposureSummary) {
            CovidExposureSummary(
                daysSinceLastExposure,
                matchedKeyCount,
                (maximumRiskScore * 8.0 / 4096).toInt(),
                attenuationDurationsInMinutes,
                (summationRiskScore * 8.0 / 4096).toInt()
            )
        }

    override fun covidExposureInformation(
        exposureInformation: ExposureInformation
    ) =
        with(exposureInformation) {
            CovidExposureInformation(
                date = Date(dateMillisSinceEpoch),
                duration = durationMinutes,
                attenuationValue = attenuationValue,
                transmissionRiskLevel = transmissionRiskLevel,
                totalRiskScore = computeRiskScore(
                    attenuationDurationsInMinutes,
                    transmissionRiskLevel
                ),
                attenuationDurations = attenuationDurationsInMinutes.toList()
            )
        }
}