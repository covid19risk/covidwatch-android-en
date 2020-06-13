package org.covidwatch.android.ui

import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.databinding.BindingAdapter
import org.covidwatch.android.R
import org.covidwatch.android.data.CovidExposureInformation
import org.covidwatch.android.data.CovidExposureSummary
import org.covidwatch.android.data.RiskScoreLevel
import org.covidwatch.android.data.RiskScoreLevel.*
import org.covidwatch.android.data.level
import org.covidwatch.android.ui.util.DateFormatter
import java.util.*

@BindingAdapter("exposureSummary")
fun TextView.setExposureSummary(exposureSummary: CovidExposureSummary?) {
    exposureSummary?.let {
        text = context.getString(
            R.string.exposure_summary,
            it.daySinceLastExposure,
            it.matchedKeyCount,
            it.maximumRiskScore
        )
    }
    if (exposureSummary == null) {
        text = context.getString(R.string.no_exposure)
    }
}

@BindingAdapter("exposure")
fun TextView.setTextFromExposure(exposure: CovidExposureInformation?) {
    exposure?.let {
        when (it.totalRiskScore.level) {
            HIGH -> {
                setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_risk_high,
                    0,
                    0,
                    0
                )
                text = HtmlCompat.fromHtml(
                    context.getString(
                        R.string.high_risk_exposure,
                        DateFormatter.format(it.date)
                    ),
                    FROM_HTML_MODE_COMPACT
                )
            }
            MEDIUM -> {
                setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_risk_med,
                    0,
                    0,
                    0
                )
                text = HtmlCompat.fromHtml(
                    context.getString(
                        R.string.med_risk_exposure,
                        DateFormatter.format(it.date)
                    ),
                    FROM_HTML_MODE_COMPACT
                )
            }
            NONE,
            LOW -> {
                setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_risk_low,
                    0,
                    0,
                    0
                )
                text = HtmlCompat.fromHtml(
                    context.getString(
                        R.string.low_risk_exposure,
                        DateFormatter.format(it.date)
                    ),
                    FROM_HTML_MODE_COMPACT
                )
            }
        }
    }
}

@BindingAdapter("attenuation_durations")
fun TextView.setTextFromAttenuationDurations(attenuationDurations: List<Int>?) {
    text = attenuationDurations?.joinToString { "${it}m" }
}

@BindingAdapter("total_risk")
fun TextView.setTextFromTotalRisk(totalRiskScore: Int?) {
    totalRiskScore?.let {
        text = context.getString(R.string.exposure_information_transmission_risk_text, it)
    }
}

@BindingAdapter("date")
fun TextView.setTextFromTime(time: Date?) {
    time ?: return
    text = DateFormatter.format(time)
}

@BindingAdapter("exposure_details_date")
fun TextView.setExposureInfoDate(time: Date?) {
    time ?: return
    text = HtmlCompat.fromHtml(
        context.getString(
            R.string.exposure_date_and_info,
            DateFormatter.format(time)
        ), FROM_HTML_MODE_COMPACT
    )
}

@BindingAdapter("last_exposure_time")
fun TextView.setTextFromLastExposureTime(time: Date?) {
    time ?: return
    text = HtmlCompat.fromHtml(
        context.getString(R.string.last_exposure_time, DateFormatter.formatDateAndTime(time)),
        FROM_HTML_MODE_COMPACT
    )
}

@BindingAdapter("risk_level")
fun TextView.setRiskLevelText(riskScoreLevel: RiskScoreLevel) {
    setText(
        when (riskScoreLevel) {
            HIGH -> R.string.high_risk_title
            MEDIUM -> R.string.med_risk_title
            NONE,
            LOW -> R.string.low_risk_title
        }
    )
}

@BindingAdapter("background_risk_level")
fun View.setBackgroundFromRiskLevel(riskScoreLevel: RiskScoreLevel) {
    background = context.getDrawable(
        when (riskScoreLevel) {
            HIGH -> R.color.high_risk
            MEDIUM -> R.color.med_risk
            NONE,
            LOW -> R.color.low_risk
        }
    )
}

