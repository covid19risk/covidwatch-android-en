package org.covidwatch.android.extension

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.covidwatch.android.exposurenotification.ENStatus
import org.covidwatch.android.exposurenotification.Status
import org.covidwatch.android.functional.Either
import java.util.concurrent.ExecutionException

suspend fun <T> Task<T>.await(): Either<Int, T> = withContext(Dispatchers.IO) {
    try {
        Either.Right(Tasks.await(this@await))
    } catch (e: ExecutionException) {
        val apiException = e.cause as? ApiException
        val status = apiException?.statusCode ?: -1
        Either.Left(status)
    } catch (e: Exception) {
        Either.Left(Status.FAILED_INTERNAL)
    }
}

suspend fun Task<Void>.awaitNoResult(): Either<ENStatus, Void?> = await().let {
    val result = it.right
    val status = it.left
    return if (status != null) {
        Either.Left(ENStatus(status))
    } else {
        Either.Right(result)
    }
}

suspend fun <T> Task<T>.awaitWithStatus(): Either<ENStatus, T> = await().let {
    val result = it.right
    val status = it.left
    return if (result != null) {
        Either.Right(result)
    } else {
        Either.Left(ENStatus(status))
    }
}