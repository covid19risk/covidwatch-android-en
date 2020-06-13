package org.covidwatch.android.data

import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.Expose
import org.covidwatch.android.R
import org.covidwatch.android.data.converter.AttenuationDurationsConverter
import java.io.Serializable

@Entity(tableName = "exposure_information")
@TypeConverters(value = [AttenuationDurationsConverter::class])
data class CovidExposureInformation(
    @Expose
    val dateMillisSinceEpoch: Long,
    @Expose
    val durationMinutes: Int,
    @Expose
    val attenuationValue: Int,
    @Expose
    val transmissionRiskLevel: Int,
    @Expose
    val totalRiskScore: RiskScore,
    @Expose
    val attenuationDurations: List<Int>,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
) : Serializable {

    @Ignore
    @StringRes
    val howClose = when (attenuationValue) {
        in 0..100 -> R.string.far_exposure_distance
        in 101..200 -> R.string.close_exposure_distance
        else -> R.string.near_exposure_distance
    }

    @Ignore
    val riskScoreLevel = totalRiskScore.level

    @Ignore
    val highRisk = riskScoreLevel == RiskScoreLevel.HIGH
}