package org.covidwatch.android.ui.menu

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.gson.GsonBuilder
import com.jaredrummler.android.device.DeviceName
import kotlinx.coroutines.launch
import org.covidwatch.android.BuildConfig
import org.covidwatch.android.DateFormatter
import org.covidwatch.android.R
import org.covidwatch.android.attenuationDurationThresholds
import org.covidwatch.android.data.exposureinformation.ExposureInformationRepository
import org.covidwatch.android.data.pref.PreferenceStorage
import org.covidwatch.android.databinding.DialogPossibleExposuresTestCaseBinding
import org.covidwatch.android.databinding.FragmentMenuBinding
import org.covidwatch.android.domain.ProvideDiagnosisKeysUseCase
import org.covidwatch.android.extension.observe
import org.covidwatch.android.extension.observeUseCase
import org.covidwatch.android.ui.BaseViewModelFragment
import org.koin.android.ext.android.inject
import java.io.File
import java.util.*


open class BaseMenuFragment : BaseViewModelFragment<FragmentMenuBinding, MenuViewModel>() {
    private val exposureInformationRepository: ExposureInformationRepository by inject()
    private val provideDiagnosisKeysUseCase: ProvideDiagnosisKeysUseCase by inject()
    private val preferences: PreferenceStorage by inject()

    override val viewModel: MenuViewModel by inject()

    override fun bind(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentMenuBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MenuAdapter { handleMenuItemClick(it) }
        with(binding) {
            menuList.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )

            menuList.adapter = adapter
            closeButton.setOnClickListener { findNavController().popBackStack() }
        }

        observe(viewModel.highRiskExposure) {
            if (it) {
                adapter.showHighRiskPossibleExposures()
            } else {
                adapter.showNoRiskPossibleExposures()
            }
        }
    }

    open fun handleMenuItemClick(menuItem: MenuItem) {
        when (menuItem.destination) {
            is PossibleExposures -> findNavController().navigate(R.id.exposuresFragment)
            is NotifyOthers -> findNavController().navigate(R.id.notifyOthersFragment)
            is HowItWorks -> findNavController().navigate(R.id.onboardingFragment)
            is Browser -> openBrowser(getString(menuItem.destination.url))
        }
        // TODO: 10.07.2020 Remove demo functionality from prod version
        when (menuItem.title) {
            R.string.menu_reset_possible_exposures -> {
                lifecycleScope.launch {
                    exposureInformationRepository.reset()
                    preferences.resetExposureSummary()
                }
                Toast.makeText(context, "Possible exposures were deleted", Toast.LENGTH_SHORT)
                    .show()
                findNavController().popBackStack()
            }
            R.string.menu_detect_exposures_from_server -> {
                lifecycleScope.observeUseCase(provideDiagnosisKeysUseCase) {
                    success {
                        Toast.makeText(
                            context,
                            "Positive diagnosis downloaded",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                    failure { handleStatus(it) }
                    findNavController().popBackStack()
                }
            }
            R.string.menu_export_possible_exposures -> {
                context?.let { context ->
                    val dialogView =
                        DialogPossibleExposuresTestCaseBinding.inflate(LayoutInflater.from(context))
                    val dialog = AlertDialog
                        .Builder(context)
                        .setView(dialogView.root)
                        .setPositiveButton(R.string.btn_continue) { _, _ ->
                            sharePossibleExposures(context, dialogView.testCaseName.text.toString())
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .create()
                    dialog.show()

                }
            }
        }
    }

    private fun sharePossibleExposures(context: Context, testCaseName: String) {
        lifecycleScope.launch {
            // Create JSON of possible exposures
            val exposures = exposureInformationRepository.exposures()
            val configuration = preferences.exposureConfiguration.asCovidExposureConfiguration()

            val possibleExposuresJson = PossibleExposuresJson(
                configuration.copy(attenuationDurationThresholdList = attenuationDurationThresholds),
                exposures
            )

            val json = GsonBuilder()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .create()
                .toJson(possibleExposuresJson)

            // Write Json to a file
            val fileName =
                "${DeviceName.getDeviceName()}_${DateFormatter.format(Date())}_$testCaseName.json"
            val file = File(context.filesDir, fileName)

            json.byteInputStream().copyTo(file.outputStream())
            val uri = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".fileprovider",
                file
            )

            // Share the file
            ShareCompat.IntentBuilder.from(requireActivity())
                .setEmailTo(arrayOf("calibration-test@covidwatch.org"))
                .setSubject(fileName)
                .setStream(uri)
                .setType("text/json")
                .startChooser()
        }
    }

    private fun openBrowser(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }
}
