package org.covidwatch.android.ui.exposures

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import org.covidwatch.android.databinding.FragmentExposuresBinding
import org.covidwatch.android.ui.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExposuresFragment : BaseFragment<FragmentExposuresBinding>() {

    private val exposuresViewModel: ExposuresViewModel by viewModel()

    override fun binding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentExposuresBinding = FragmentExposuresBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            viewModel = exposuresViewModel
            btnClose.setOnClickListener {
                findNavController().popBackStack()
            }
            enableExposureNotification.setOnCheckedChangeListener { _, _ ->
                exposuresViewModel.toggleExposureNotifications()
            }
        }
    }
}
