package org.covidwatch.android.ui.reporting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.covidwatch.android.R
import org.covidwatch.android.data.NextStepType
import org.covidwatch.android.data.pref.PreferenceStorage
import org.covidwatch.android.databinding.DialogTestVerificationCodeInfoBinding
import org.covidwatch.android.databinding.ItemVerificationCodeStepBinding
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
            tvRegion.text = HtmlCompat.fromHtml(
                getString(R.string.current_region, region.name),
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )

            val layoutInflater = LayoutInflater.from(context)
            verificationCodeSteps.removeAllViews()
            // TODO: 17.07.2020 Remove the source of the steps to be the right one
            region.nextStepsRiskHigh.filter { it.type == NextStepType.PHONE }.forEach { step ->
                val stepView = ItemVerificationCodeStepBinding.inflate(
                    layoutInflater,
                    verificationCodeSteps,
                    true
                )

                stepView.verificationCodeStep.text = step.description
                stepView.btnCall.setOnClickListener {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse(step.url)
                    }
                    if (intent.resolveActivity(view.context.packageManager) != null) {
                        startActivity(intent)
                    }
                }
            }

            tvRegion.setOnClickListener { findNavController().navigate(R.id.selectRegionFragment) }

            closeButton.setOnClickListener { dismiss() }
        }
    }
}