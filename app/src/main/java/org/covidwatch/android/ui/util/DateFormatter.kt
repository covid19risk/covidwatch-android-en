package org.covidwatch.android.ui.util

import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {
    private const val DATE_PATTERN = "MMM dd, yyyy"
    private const val TEST_DATE_PATTERN = "mm-dd-yyyy"
    private const val DATE_TIME_PATTERN = "MMM dd, yyyy, hh:mm aaa"

    private var locale = Locale.getDefault()

    private var dateFormat = SimpleDateFormat(DATE_PATTERN, locale)
        get() {
            if (locale == Locale.getDefault()) {
                return field
            }
            locale = Locale.getDefault()
            return SimpleDateFormat(DATE_PATTERN, locale).also { field = it }
        }

    private var dateAndTimeFormat = SimpleDateFormat(DATE_TIME_PATTERN, locale)
        get() {
            if (locale == Locale.getDefault()) {
                return field
            }
            locale = Locale.getDefault()
            return SimpleDateFormat(DATE_TIME_PATTERN, locale).also { field = it }
        }

    private var testDateFormat = SimpleDateFormat(TEST_DATE_PATTERN, Locale.US)

    @JvmStatic
    fun format(time: Date?): String = time?.let { dateFormat.format(it) } ?: ""

    @JvmStatic
    fun format(time: Long?): String = time?.let { dateFormat.format(it) } ?: ""

    fun formatTestDate(time: Long?): String = time?.let { testDateFormat.format(it) } ?: ""

    fun testDate(date: String?): Date = date?.let { testDateFormat.parse(it) } ?: Date()

    @JvmStatic
    fun formatDateAndTime(time: Date?): String = time?.let { dateAndTimeFormat.format(it) } ?: ""
}