package org.covidwatch.android.ui.util

import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {
    private const val DATE_PATTERN = "MMM dd, yyyy"
    private const val DATE_TIME_PATTERN = "MMM dd, yyyy, hh:mm aaa"

    private var locale = Locale.getDefault()
    private var date = SimpleDateFormat(DATE_PATTERN, locale)
        get() {
            if (locale == Locale.getDefault()) {
                return field
            }
            locale = Locale.getDefault()
            return SimpleDateFormat(DATE_PATTERN, locale).also { field = it }
        }

    private var dateAndTime = SimpleDateFormat(DATE_TIME_PATTERN, locale)
        get() {
            if (locale == Locale.getDefault()) {
                return field
            }
            locale = Locale.getDefault()
            return SimpleDateFormat(DATE_TIME_PATTERN, locale).also { field = it }
        }

    fun format(time: Long?): String = date.format(time)

    fun formatDateAndTime(time: Long?): String = dateAndTime.format(time)
}