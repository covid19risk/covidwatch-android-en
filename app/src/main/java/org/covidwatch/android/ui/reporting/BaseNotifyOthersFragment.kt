package org.covidwatch.android.ui.reporting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import org.covidwatch.android.R
import org.covidwatch.android.databinding.DialogPastPositiveDiagnosesBinding
import org.covidwatch.android.databinding.DialogTestVerificationCodeInfoBinding
import org.covidwatch.android.databinding.FragmentNotifyOthersBinding
import org.covidwatch.android.extension.observe
import org.covidwatch.android.extension.observeEvent
import org.covidwatch.android.ui.BaseViewModelFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


open class BaseNotifyOthersFragment :
    BaseViewModelFragment<FragmentNotifyOthersBinding, NotifyOthersViewModel>() {

    override val viewModel: NotifyOthersViewModel by viewModel()

    private val adapter = PositiveDiagnosisAdapter()

    override fun bind(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNotifyOthersBinding =
        FragmentNotifyOthersBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            closeButton.setOnClickListener {
                findNavController().popBackStack()
            }

            ivTestVerificationCodeInfo.setOnClickListener {
                val context = requireContext()
                val dialogView =
                    DialogTestVerificationCodeInfoBinding.inflate(LayoutInflater.from(context))

                val dialog = android.app.AlertDialog.Builder(context)
                    .setView(dialogView.root)
                    .create()
                dialogView.closeButton.setOnClickListener { dialog.dismiss() }
                dialog.show()
            }

            sharePositiveDiagnosisButton.setOnClickListener {
                viewModel.sharePositiveDiagnosis()
            }

            btnViewPastPositiveDiagnoses.setOnClickListener {
                if (adapter.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        R.string.no_past_positive_diagnoses,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val dialogView =
                    DialogPastPositiveDiagnosesBinding.inflate(LayoutInflater.from(context))
                dialogView.pastPositiveDiagnosesList.addItemDecoration(dividerItemDecoration())
                dialogView.pastPositiveDiagnosesList.adapter = adapter
                val dialog = AlertDialog
                    .Builder(requireContext())
                    .setView(dialogView.root)
                    .create()
                dialogView.closeButton.setOnClickListener { dialog.dismiss() }
                dialog.show()
            }
        }

        with(viewModel) {
            observe(positiveDiagnosis) { adapter.setItems(it) }
            observeEvent(openVerificationScreen) { findNavController().navigate(R.id.verifyPositiveDiagnosisFragment) }
        }
    }

    private fun dividerItemDecoration(): RecyclerView.ItemDecoration {
        return DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
    }
}