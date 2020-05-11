package org.covidwatch.android.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.covidwatch.android.*
import org.covidwatch.android.databinding.FragmentHomeBinding
import org.covidwatch.android.ui.BaseFragment
import org.covidwatch.android.ui.event.EventObserver
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val homeViewModel: HomeViewModel by viewModel()

    override fun binding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding =
        FragmentHomeBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Add meta-data test here
        getFirebaseIdIfTester()
        homeViewModel.onStart()
        homeViewModel.navigateToOnboardingEvent.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(R.id.splashFragment)
        })
        homeViewModel.infoBannerState.observe(viewLifecycleOwner, Observer { banner ->
            when (banner) {
                is InfoBannerState.Visible -> {
                    binding.infoBanner.isVisible = true
                    binding.infoBanner.setText(banner.text)
                }
                InfoBannerState.Hidden -> {
                    binding.infoBanner.isVisible = false
                }
            }
        })
        homeViewModel.warningBannerState.observe(viewLifecycleOwner, Observer { banner ->
            when (banner) {
                is WarningBannerState.Visible -> {
                    binding.warningBanner.isVisible = true
                    binding.warningBanner.setText(banner.text)
                }
                WarningBannerState.Hidden -> {
                    binding.warningBanner.isVisible = false
                }
            }
        })
        homeViewModel.userTestedPositive.observe(viewLifecycleOwner, Observer {
            updateUiForTestedPositive()
        })

        initClickListeners()
    }

    private fun initClickListeners() {
        binding.notifyOthersButton.setOnClickListener {
            findNavController().navigate(R.id.testQuestionsFragment)
        }
        binding.toolbar.setOnMenuItemClickListener {
            if (R.id.action_menu == it.itemId) {
                findNavController().navigate(R.id.menuFragment)
            }
            true
        }
        binding.shareAppButton.setOnClickListener {
            shareApp()
        }
        binding.warningBanner.setOnClickListener {
            findNavController().navigate(R.id.potentialRiskFragment)
        }
        binding.infoBanner.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }
        binding.exposures.dummyContainer.setOnClickListener {

        }
    }

    private fun shareApp() {
        val shareText = getString(R.string.share_intent_text)
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "$shareText https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
        )
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_text)))
    }

    private fun updateUiForTestedPositive() {
        binding.notifyOthersButton.isVisible = false
        binding.notifyOthersButtonText.isVisible = false
    }

    private fun getFirebaseIdIfTester() {
        if (BuildConfig.FIREBASE_DEBUGGING == false) {
            binding.testerId.visibility = View.GONE
            setTester(false)
            return
        } else {
            val firebaseId: String = getFirebaseId()
            binding.testerId.text = "Your COVID Watch Tester Id is: " + firebaseId
            binding.testerId.visibility = View.VISIBLE
            setTester(true)
            val context = requireContext()
            setAnalyticsInstanceFromContext(context)
            //Test event
            sendEvent("TestEvent")
            return
        }
    }
}