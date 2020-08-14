package org.covidwatch.android.ui.reporting

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import org.covidwatch.android.R
import org.covidwatch.android.databinding.FragmentVerifyPositiveDiagnosisBinding
import org.covidwatch.android.extension.observe
import org.covidwatch.android.extension.observeEvent
import org.covidwatch.android.extension.observeNullableEvent
import org.covidwatch.android.ui.BaseViewModelFragment
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import java.time.LocalDate
import java.time.ZoneId

class VerifyPositiveDiagnosisFragment :
    BaseViewModelFragment<FragmentVerifyPositiveDiagnosisBinding, VerifyPositiveDiagnosisViewModel>() {

    override val viewModel: VerifyPositiveDiagnosisViewModel by stateViewModel()

    override fun bind(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVerifyPositiveDiagnosisBinding =
        FragmentVerifyPositiveDiagnosisBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            closeButton.setOnClickListener { findNavController().popBackStack() }

            cbNoSymptoms.setOnCheckedChangeListener { _, noSymptoms ->
                disableInput(tilSymptomsDate, noSymptoms)

                noSymptomsLayout.isVisible = noSymptoms
                viewModel.noSymptoms(noSymptoms)
            }

            cbNoExposedDate.setOnCheckedChangeListener { _, noExposedDate ->
                disableInput(tilInfectionDate, noExposedDate)
                viewModel.noInfectionDate(noExposedDate)
            }

            ivTestVerificationCodeInfo.setOnClickListener {
                VerificationCodeHelpDialog().show(childFragmentManager, null)
            }
            etVerificationCode.addTextChangedListener(afterTextChanged = {
                viewModel.verificationCode(it.toString())
            })

            etSymptomsDate.setOnClickListener { viewModel.selectSymptomsDate() }
            etTestedDate.setOnClickListener { viewModel.selectTestDate() }
            etInfectionDate.setOnClickListener { viewModel.selectInfectionDate() }

            btnFinishVerification.setOnClickListener { viewModel.sharePositiveDiagnosis() }
        }

        with(viewModel) {
            observe(readyToSubmit) {
                binding.btnFinishVerification.isVisible = it
            }

            observeNullableEvent(selectSymptomsDate) { currentSelection ->
                showDatePicker(currentSelection) { viewModel.symptomDate(it) }
            }
            observeNullableEvent(selectInfectionDate) { currentSelection ->
                showDatePicker(currentSelection) { viewModel.infectionDate(it) }
            }
            observeEvent(selectTestDate) { currentSelection ->
                showDatePicker(
                    currentSelection.first,
                    currentSelection.second
                ) { viewModel.testDate(it) }
            }

            observe(infectionDateFormatted) { binding.etInfectionDate.setText(it) }
            observe(testDateFormatted) { binding.etTestedDate.setText(it) }
            observe(symptomDateDateFormatted) { binding.etSymptomsDate.setText(it) }

            observe(uploading) {
                binding.uploadProgress.isVisible = it
                // Disable the button while we uploading
                binding.btnFinishVerification.isEnabled = !it
            }
            observeEvent(showThankYou) {
                findNavController().popBackStack(R.id.homeFragment, false)
                findNavController().navigate(R.id.sharedDiagnosisFragment)
            }
        }
    }

    private fun disableInput(
        textInputLayout: TextInputLayout,
        disable: Boolean
    ) {
        textInputLayout.isEnabled = !disable
        textInputLayout.boxBackgroundColor = if (disable) ContextCompat.getColor(
            requireContext(),
            R.color.transparentGray
        ) else -1
    }

    private fun showDatePicker(
        selection: Long?,
        dayInPast: Long? = null,
        selectedDate: (Long) -> Unit
    ) {
        val builder = MaterialDatePicker.Builder.datePicker()
        val constraints = CalendarConstraints.Builder()

        val twoWeeksAgo = LocalDate.now().plusDays(-14).atStartOfDay(ZoneId.of("UTC")).toInstant()
            .toEpochMilli()

        // Day in the past or 14 days back
        val fromWhen = dayInPast ?: twoWeeksAgo

        val untilNow = LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()

        constraints.setValidator(BaseDateValidator { it in fromWhen..untilNow })

        val datePicker = builder
            .setSelection(selection)
            .setCalendarConstraints(constraints.build())
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDate(it) }
        datePicker.show(childFragmentManager, null)
    }

    @SuppressLint("ParcelCreator")
    internal class BaseDateValidator(private val _isValid: (Long) -> Boolean) :
        CalendarConstraints.DateValidator {

        override fun writeToParcel(dest: Parcel?, flags: Int) = Unit

        override fun isValid(date: Long) = _isValid(date)

        override fun describeContents() = 0
    }
}