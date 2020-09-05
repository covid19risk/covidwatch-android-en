package org.covidwatch.android.data.exposureinformation

import org.covidwatch.android.data.model.CovidExposureInformation

class ExposureInformationLocalSource(private val dao: ExposureInformationDao) {
    suspend fun saveExposureInformation(exposureInformation: List<CovidExposureInformation>) {
        dao.saveExposureInformation(exposureInformation)
    }

    fun exposureInformation() = dao.exposureInformation()

    suspend fun exposures() = dao.exposures()

    suspend fun reset() {
        dao.reset()
    }

    suspend fun deleteOlderThan(date: Long) = dao.deleteOlderThan(date)
}