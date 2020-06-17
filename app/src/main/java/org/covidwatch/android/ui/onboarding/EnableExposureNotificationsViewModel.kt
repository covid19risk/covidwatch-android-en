package org.covidwatch.android.ui.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.covidwatch.android.data.FirstTimeUser
import org.covidwatch.android.data.UserFlowRepository
import org.covidwatch.android.exposurenotification.ExposureNotificationManager
import org.covidwatch.android.extension.send
import org.covidwatch.android.ui.BaseViewModel
import org.covidwatch.android.ui.event.Event

class EnableExposureNotificationsViewModel(
    private val enManager: ExposureNotificationManager,
    private val userFlowRepository: UserFlowRepository
) : BaseViewModel() {

    private val _showHome = MutableLiveData<Event<Unit>>()
    val showHome: LiveData<Event<Unit>> = _showHome

    private val _showOnboardingFinish = MutableLiveData<Event<Unit>>()
    val showOnboardingFinish: LiveData<Event<Unit>> = _showOnboardingFinish

    fun onEnableClicked() {
        viewModelScope.launch {
            withPermission(ExposureNotificationManager.PERMISSION_START_REQUEST_CODE) {
                enManager.start().apply {
                    success { handleFirstOrReturnUser() }
                    failure { handleStatus(it) }
                }
            }
        }
    }

    fun notNowClicked() {
        handleFirstOrReturnUser()
    }

    private fun handleFirstOrReturnUser() {
        if (userFlowRepository.getUserFlow() == FirstTimeUser) {
            _showOnboardingFinish.send()
            userFlowRepository.finishOnboarding()
        } else {
            _showHome.send()
        }
    }
}