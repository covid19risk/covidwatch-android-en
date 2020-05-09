package org.covidwatch.android.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.android.gms.nearby.exposurenotification.ExposureInformation
import org.covidwatch.android.R

@Entity(tableName = "exposure_information")
data class CovidExposureInformation(
    val dateMillisSinceEpoch: Long,
    val durationMinutes: Int,
    val attenuationValue: Int,
    val transmissionRiskLevel: Int,
    val totalRiskScore: Int,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
) {

    @Ignore
    val howClose = when (attenuationValue) {
        in 0..100 -> R.string.far_exposure_distance
        in 101..200 -> R.string.close_exposure_distance
        else -> R.string.near_exposure_distance
    }

    //TODO: Check what is our risk level for showing the high risk icon
    @Ignore
    val highRisk = totalRiskScore > 6
}

fun ExposureInformation.toCovidExposureInformation() = CovidExposureInformation(
    dateMillisSinceEpoch,
    durationMinutes,
    attenuationValue,
    transmissionRiskLevel,
    totalRiskScore
)