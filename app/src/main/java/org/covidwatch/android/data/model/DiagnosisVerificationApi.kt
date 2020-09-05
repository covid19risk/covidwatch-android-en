package org.covidwatch.android.data.model

import com.google.gson.annotations.SerializedName

data class VerificationCertificateRequest(
    val token: String,
    @SerializedName("ekeyhmac")
    val exposureKeyHmac: String
)

data class VerificationCertificateResponse(
    val certificate: String?,
    val error: String?
)

data class VerifyCodeRequest(
    val code: String
)

data class VerifyCodeResponse(
    @SerializedName("testtype")
    val testType: String?,
    val symptomDate: String?,
    val token: String?,
    val error: String?
)

object TestType {
    const val CONFIRMED = "confirmed"
    const val LIKELY = "likely"
    const val NEGATIVE = "negative"
}