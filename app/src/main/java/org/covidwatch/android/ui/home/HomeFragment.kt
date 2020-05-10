package org.covidwatch.android.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.covidwatch.android.*
import org.covidwatch.android.data.FirstTimeUser
import org.covidwatch.android.data.ReturnUser
import org.covidwatch.android.data.Setup
import org.covidwatch.android.databinding.FragmentHomeBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Add meta-data test here
        getFirebaseIdIfTester()
        binding.swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorPrimary
            )
        )
        homeViewModel.onStart()
        homeViewModel.userFlow.observe(viewLifecycleOwner, Observer { userFlow ->
            when (userFlow) {
                is FirstTimeUser -> {
                    updateUiForFirstTimeUser()
                }
                is Setup -> {
                    findNavController().navigate(R.id.splashFragment)
                }
                is ReturnUser -> {
                    updateUiForReturnUser()
                }
            }
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
        homeViewModel.isRefreshing.observe(viewLifecycleOwner, Observer { isRefreshing ->
            binding.swipeRefreshLayout.isRefreshing = isRefreshing
        })

        initClickListeners()
    }

    private fun initClickListeners() {
        binding.testedButton.setOnClickListener {
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
        binding.swipeRefreshLayout.setOnRefreshListener {
            homeViewModel.onRefreshRequested()
        }
        //TODO: Remove
        binding.testDelete.setOnClickListener { findNavController().navigate(R.id.possibleExposures) }
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

    private fun updateUiForFirstTimeUser() {
        binding.homeTitle.setText(R.string.you_re_all_set_title)
        binding.homeSubtitle.setText(R.string.thank_you_text)
    }

    private fun updateUiForReturnUser() {
        binding.homeTitle.setText(R.string.welcome_back_title)
        binding.homeSubtitle.setText(R.string.not_detected_text)
    }

    private fun updateUiForTestedPositive() {
        binding.homeSubtitle.setText(R.string.reported_tested_positive_text)
        binding.testedButton.isVisible = false
        binding.testedButtonText.isVisible = false
    }

    private fun getFirebaseIdIfTester() {
        if (BuildConfig.FIREBASE_DEBUGGING == false) {
            binding.testerId.visibility = View.GONE
            setTester(false)
            return;
        } else {
            val firebaseId: String = getFirebaseId()
            binding.testerId.setText("Your COVID Watch Tester Id is: " + firebaseId)
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