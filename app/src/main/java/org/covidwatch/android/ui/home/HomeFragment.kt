package org.covidwatch.android.ui.home

import android.animation.LayoutTransition
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import org.covidwatch.android.R
import org.covidwatch.android.data.NextStep
import org.covidwatch.android.data.NextStepType
import org.covidwatch.android.data.RiskLevel
import org.covidwatch.android.databinding.FragmentHomeBinding
import org.covidwatch.android.databinding.ItemNextStepBinding
import org.covidwatch.android.extension.fromHtml
import org.covidwatch.android.extension.observe
import org.covidwatch.android.extension.observeEvent
import org.covidwatch.android.extension.shareApp
import org.covidwatch.android.ui.BaseFragment
import org.covidwatch.android.ui.logo
import org.covidwatch.android.ui.name
import org.covidwatch.android.ui.setBackgroundFromRiskLevel
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModel()

    override fun bind(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding =
        FragmentHomeBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            onStart()
            observeEvent(navigateToOnboarding) {
                findNavController().popBackStack()
                findNavController().navigate(R.id.splashFragment)
            }

            observe(region) {
                binding.tvRegion.text = HtmlCompat.fromHtml(
                    getString(R.string.current_region, it.name),
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
                binding.toolbar.logo = ContextCompat.getDrawable(view.context, it.logo)
            }

            observe(riskLevel) {
                // TODO: 22.07.2020 Make this logic more clear and separate for different view groups
                when (it) {
                    RiskLevel.VERIFIED_POSITIVE -> {
                        binding.actionLayoutTitle.isVisible = false
                        binding.actionLayoutInfo.isVisible = false
                        binding.actionLayoutBtn.isVisible = false

                        binding.riskInfo.isVisible = true
                        binding.riskInfo.text = getString(R.string.next_steps_title).fromHtml()
                    }
                    RiskLevel.HIGH -> {
                        binding.actionLayoutTitle.isVisible = true
                        binding.actionLayoutInfo.isVisible = true
                        binding.actionLayoutBtn.isVisible = true

                        binding.riskInfo.isVisible = true
                        binding.riskInfo.text = getString(R.string.next_steps_title).fromHtml()
                    }
                    RiskLevel.LOW -> {

                        binding.actionLayoutTitle.isVisible = true
                        binding.actionLayoutInfo.isVisible = true
                        binding.actionLayoutBtn.isVisible = true

                        binding.riskInfo.isVisible = true
                        binding.riskInfo.text = getString(R.string.unknown_risk_title).fromHtml()
                    }
                    RiskLevel.DISABLED -> {
                        binding.actionLayoutTitle.isVisible = false
                        binding.actionLayoutInfo.isVisible = false
                        binding.actionLayoutBtn.isVisible = false

                        binding.riskInfo.isVisible = false
                    }
                }

                binding.actionLayoutBtn.setOnClickListener {
                    findNavController().navigate(R.id.notifyOthersFragment)
                }
                binding.myRiskLevel.setBackgroundFromRiskLevel(it)
                binding.myRiskLevel.text =
                    getString(R.string.my_risk_level, it.name(requireContext()))
            }
            observe(nextSteps, ::bindNextSteps)
            observeEvent(showOnboardingAnimation) {
                lifecycleScope.launchWhenResumed {
                    binding.homeScreenArt.isVisible = false
                    binding.homeScreenContent.layoutTransition = LayoutTransition()
                    delay(1000)
                    binding.homeScreenArt.isVisible = true
                }
            }

            observe(infoBannerState) { banner ->
                when (banner) {
                    is InfoBannerState.Visible -> {
                        binding.infoBanner.isVisible = true
                        binding.infoBanner.setText(banner.text)
                    }
                    InfoBannerState.Hidden -> {
                        binding.infoBanner.isVisible = false
                    }
                }
            }
        }


        initClickListeners()
    }

    private fun initClickListeners() {
        with(binding) {
            menu.setOnClickListener {
                findNavController().navigate(R.id.menuFragment)
            }
            myRiskLevel.setOnClickListener {
                findNavController().navigate(R.id.exposuresFragment)
            }
            tvRegion.setOnClickListener { findNavController().navigate(R.id.selectRegionFragment) }
            infoBanner.setOnClickListener {
                findNavController().navigate(R.id.enableExposureNotificationsFragment)
            }
        }
    }

    private fun bindNextSteps(nextSteps: List<NextStep>) {
        val layoutInflater = LayoutInflater.from(context)
        binding.nextSteps.removeAllViews()
        nextSteps.forEach { nextStep ->
            val view = ItemNextStepBinding.inflate(layoutInflater, binding.nextSteps, true)
            with(view) {
                nextStepText.text = nextStep.description
                when (nextStep.type) {
                    NextStepType.INFO -> {
                        nextStepIcon.setImageResource(R.drawable.ic_info_filled)
                    }
                    NextStepType.PHONE -> {
                        nextStepIcon.setImageResource(R.drawable.ic_next_step_phone)
                        root.addCircleRipple()
                        root.setOnClickListener {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse(nextStep.url)
                            }
                            if (intent.resolveActivity(view.root.context.packageManager) != null) {
                                startActivity(intent)
                            }
                        }
                    }
                    NextStepType.WEBSITE -> {
                        root.addCircleRipple()
                        root.setOnClickListener {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(nextStep.url))
                            startActivity(browserIntent)
                        }
                        nextStepIcon.setImageResource(R.drawable.ic_next_step_web)
                    }
                    NextStepType.SHARE -> {
                        root.addCircleRipple()
                        root.setOnClickListener {
                            context?.shareApp()
                        }
                        nextStepIcon.setImageResource(R.drawable.ic_next_step_share)
                    }
                    NextStepType.SELECT_REGION -> {
                        root.addCircleRipple()
                        root.setOnClickListener {
                            findNavController().navigate(R.id.selectRegionFragment)
                        }
                        nextStepIcon.setImageResource(R.drawable.ic_region)
                    }
                }
            }
        }
    }

    private fun View.addCircleRipple() = with(TypedValue()) {
        context.theme.resolveAttribute(
            android.R.attr.selectableItemBackgroundBorderless,
            this,
            true
        )
        foreground = ContextCompat.getDrawable(requireContext(), resourceId)
    }
}
