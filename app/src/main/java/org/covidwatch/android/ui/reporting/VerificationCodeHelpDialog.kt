package org.covidwatch.android.ui.reporting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.covidwatch.android.R
import org.covidwatch.android.data.NextStepType
import org.covidwatch.android.data.pref.PreferenceStorage
import org.covidwatch.android.databinding.DialogTestVerificationCodeInfoBinding
import org.covidwatch.android.databinding.ItemVerificationCodeStepBinding
import org.covidwatch.android.ui.Intents.dial
import org.covidwatch.android.ui.Intents.openBrowser
import org.covidwatch.android.ui.setRegion
import org.koin.android.ext.android.inject

class VerificationCodeHelpDialog : BottomSheetDialogFragment() {
    private var _binding: DialogTestVerificationCodeInfoBinding? = null
    private val binding get() = _binding!!

    private val prefs: PreferenceStorage by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogTestVerificationCodeInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val region = prefs.region
        with(binding) {
            tvRegion.setRegion(region)

            val layoutInflater = LayoutInflater.from(context)
            verificationCodeSteps.removeAllViews()
            region.nextStepsVerificationCode.forEach { step ->
                val stepView = ItemVerificationCodeStepBinding.inflate(
                    layoutInflater,
                    verificationCodeSteps,
                    true
                )
                stepView.verificationCodeStep.text = step.description

                when (step.type) {
                    NextStepType.PHONE -> {
                        stepView.btnAction.setText(R.string.btn_call)
                        stepView.btnAction.setOnClickListener {
                            context?.dial(step.url)
                        }
                    }
                    NextStepType.WEBSITE -> {
                        stepView.btnAction.setText(R.string.btn_learn_more)
                        stepView.btnAction.setOnClickListener {
                            context?.openBrowser(step.url)
                        }
                    }
                    NextStepType.SELECT_REGION -> {
                        stepView.btnAction.setText(R.string.btn_choose_different_region)
                        stepView.btnAction.setOnClickListener {
                            findNavController().navigate(R.id.selectRegionFragment)
                        }
                    }
                    else -> stepView.btnAction.isVisible = false
                }
            }

            tvRegion.setOnClickListener { findNavController().navigate(R.id.selectRegionFragment) }

            closeButton.setOnClickListener { dismiss() }
        }
    }
}