package org.covidwatch.android.data.positivediagnosis

import org.covidwatch.android.data.PositiveDiagnosisReport

class PositiveDiagnosisLocalSource(private val reportDao: PositiveDiagnosisReportDao) {

    fun reports() = reportDao.reports()
    suspend fun addPositiveDiagnosisReport(report: PositiveDiagnosisReport) = reportDao.insert(report)
}