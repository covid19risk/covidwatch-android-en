package org.covidwatch.android.domain

import org.covidwatch.android.data.EnConverter
import org.covidwatch.android.data.diagnosiskeystoken.DiagnosisKeysTokenRepository
import org.covidwatch.android.data.exposureinformation.ExposureInformationRepository
import org.covidwatch.android.exposurenotification.ENStatus
import org.covidwatch.android.exposurenotification.ExposureNotificationManager
import org.covidwatch.android.functional.Either
import timber.log.Timber

class UpdateExposureInformationUseCase(
    private val enManager: ExposureNotificationManager,
    private val tokenRepository: DiagnosisKeysTokenRepository,
    private val exposureInformationRepository: ExposureInformationRepository,
    private val enConverter: EnConverter,
    dispatchers: AppCoroutineDispatchers
) : UseCase<Unit, Unit>(dispatchers) {
    override suspend fun run(params: Unit?): Either<ENStatus, Unit> {
        if (enManager.isEnabled().right == false) return Either.Left(ENStatus.FailedServiceDisabled)
        Timber.d("Start ${javaClass.simpleName}")

        tokenRepository.exposedTokens().forEach { keysToken ->
            Timber.d("Get Exposure Information for: $keysToken")
            enManager.getExposureInformation(keysToken.token).apply {
                success { information ->

                    val exposureInformation = information.map {
                        enConverter.covidExposureInformation(it).also { exposure ->
                            exposure.exposureConfiguration = keysToken.exposureConfiguration
                        }
                    }

                    Timber.d("Exposure Information for ${keysToken.token}: ${exposureInformation.joinToString()}")

                    exposureInformationRepository.saveExposureInformation(exposureInformation)
                    tokenRepository.delete(keysToken)
                }

                failure { status ->
                    Timber.d("Failed to get Exposure Information for ${keysToken.token}")
                    return Either.Left(status)
                }
            }
        }

        return Either.Right(Unit)
    }
}