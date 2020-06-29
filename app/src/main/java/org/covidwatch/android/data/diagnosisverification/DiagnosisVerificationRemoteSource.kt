package org.covidwatch.android.data.diagnosisverification

import androidx.annotation.WorkerThread
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.covidwatch.android.exposurenotification.ServerException

class DiagnosisVerificationRemoteSource(
    private val apiKey: String,
    private val verificationServerEndpoint: String,
    private val gson: Gson,
    private val httpClient: OkHttpClient
) {
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    @WorkerThread
    fun verify(testCode: String): String {
        val body: RequestBody = gson.toJson(VerifyCodeRequest(testCode)).toRequestBody(jsonType)

        val request = Request.Builder()
            .header("X-API-Key", apiKey)
            .post(body)
            .url("$verificationServerEndpoint/api/verify").build()

        return httpClient.newCall(request).execute().let { response ->
            if (response.code != 200) throw ServerException()

            gson.fromJson(response.body?.charStream(), VerifyCodeResponse::class.java).token!!
        }
    }

    fun certificate(token: String, hmac: String): String {
        val body: RequestBody =
            gson.toJson(VerificationCertificateRequest(token, hmac)).toRequestBody(jsonType)

        val request = Request.Builder()
            .header("X-API-Key", apiKey)
            .post(body)
            .url("$verificationServerEndpoint/api/certificate").build()

        return httpClient.newCall(request).execute().let { response ->
            if (response.code != 200) throw ServerException()

            gson.fromJson(
                response.body?.charStream(),
                VerificationCertificateResponse::class.java
            ).certificate!!
        }
    }
}
