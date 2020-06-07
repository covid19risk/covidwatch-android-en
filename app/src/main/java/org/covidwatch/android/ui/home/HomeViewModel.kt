package org.covidwatch.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.covidwatch.android.R
import org.covidwatch.android.data.CovidExposureSummary
import org.covidwatch.android.data.FirstTimeUser
import org.covidwatch.android.data.UserFlowRepository
import org.covidwatch.android.data.pref.PreferenceStorage
import org.covidwatch.android.domain.TestedRepository
import org.covidwatch.android.exposurenotification.ExposureNotificationManager
import org.covidwatch.android.ui.BaseViewModel
import org.covidwatch.android.ui.event.Event

class HomeViewModel(
    private val enManager: ExposureNotificationManager,
    private val userFlowRepository: UserFlowRepository,
    private val testedRepository: TestedRepository,
    private val preferenceStorage: PreferenceStorage
) : BaseViewModel() {

    private val _isUserTestedPositive = MutableLiveData<Boolean>()
    val isUserTestedPositive: LiveData<Boolean> get() = _isUserTestedPositive

    private val _infoBannerState = MutableLiveData<InfoBannerState>()
    val infoBannerState: LiveData<InfoBannerState> get() = _infoBannerState

    private val _warningBannerState = MutableLiveData<WarningBannerState>()
    val warningBannerState: LiveData<WarningBannerState> get() = _warningBannerState

    private val _navigateToOnboardingEvent = MutableLiveData<Event<Unit>>()
    val navigateToOnboardingEvent: LiveData<Event<Unit>> get() = _navigateToOnboardingEvent

    val exposureSummary: LiveData<CovidExposureSummary>
        get() = preferenceStorage.observableExposureSummary

    fun onStart() {
        val userFlow = userFlowRepository.getUserFlow()
        if (userFlow is FirstTimeUser) {
            _navigateToOnboardingEvent.value = Event(Unit)
            return
        }

        viewModelScope.launch {
            val enabled = enManager.isEnabled().result() ?: false
            _infoBannerState.value = if (enabled) {
                InfoBannerState.Hidden
            } else {
                InfoBannerState.Visible(R.string.turn_on_exposure_notification_text)
            }
        }
        checkIfUserTestedPositive()
    }


    private fun checkIfUserTestedPositive() {
        val isUserTestedPositive = testedRepository.isUserTestedPositive()
        _isUserTestedPositive.value = isUserTestedPositive
        if (isUserTestedPositive) {
            _warningBannerState.value = WarningBannerState.Visible(R.string.reported_alert_text)
        }
    }
}