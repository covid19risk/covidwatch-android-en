package org.covidwatch.android.ui.menu

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import org.covidwatch.android.R
import kotlinx.coroutines.GlobalScope
import org.covidwatch.android.data.CovidExposureInformation
import org.covidwatch.android.data.exposureinformation.ExposureInformationRepository
import org.covidwatch.android.data.toCovidExposureInformation
import org.covidwatch.android.exposurenotification.RandomEnObjects
import org.covidwatch.android.extension.io
import org.koin.android.ext.android.inject
import org.covidwatch.android.exposurenotification.*


class MenuFragment : Fragment(R.layout.fragment_menu) {

    val exposureInformationRepository: ExposureInformationRepository by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuList: RecyclerView = view.findViewById(R.id.menu_list)
        menuList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        menuList.adapter = MenuAdapter { destination ->
            handleMenuItemClick(destination)
        }

        val closeButton: ImageView = view.findViewById(R.id.close_button)
        closeButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun handleMenuItemClick(destination: Destination) {
        when (destination) {
            is Settings -> findNavController().navigate(R.id.settingsFragment)
            is TestResults -> {}
            is MakeTestExposureNotification -> makeTestExposureNotification()
            is Browser -> openBrowser(destination.url)
        }
    }

    private fun openBrowser(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    private fun makeTestExposureNotification() {
        exposureInformationRepository.addFakeItem()
        findNavController().navigate(R.id.homeFragment)
    }

}
