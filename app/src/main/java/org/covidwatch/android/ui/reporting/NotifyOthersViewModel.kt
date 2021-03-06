package org.covidwatch.android.ui.reporting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.covidwatch.android.extension.send
import org.covidwatch.android.ui.BaseViewModel
import org.covidwatch.android.ui.event.Event

class NotifyOthersViewModel : BaseViewModel() {

    private val _openVerificationScreen = MutableLiveData<Event<Unit>>()
    val openVerificationScreen: LiveData<Event<Unit>> = _openVerificationScreen

    fun sharePositiveDiagnosis() {
        _openVerificationScreen.send()
    }
}