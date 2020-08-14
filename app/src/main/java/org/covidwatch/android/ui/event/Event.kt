package org.covidwatch.android.ui.event

import androidx.lifecycle.Observer

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
open class Event<out T>(private val content: T) {

    private var handled = false

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (handled) {
            null
        } else {
            handled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}

class NullableEvent<out T>(private val content: T?) {

    private var handled = false

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (handled) {
            null
        } else {
            handled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T? = content
}


/**
 * An [Observer] for [NullableEvent]s, simplifying the pattern of checking if the [NullableEvent]'s content has
 * already been handled.
 *
 * [onEventUnhandledContent] is *only* called if the [NullableEvent]'s contents has not been handled.
 */
class NullableEventObserver<T>(private val onEventUnhandledContent: (T?) -> Unit) :
    Observer<NullableEvent<T?>> {
    override fun onChanged(event: NullableEvent<T?>) {
        onEventUnhandledContent(event.getContentIfNotHandled())
    }
}

/**
 * An [Observer] for [Event]s, simplifying the pattern of checking if the [Event]'s content has
 * already been handled.
 *
 * [onEventUnhandledContent] is *only* called if the [Event]'s contents has not been handled.
 */
class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>?) {
        event?.getContentIfNotHandled()?.run(onEventUnhandledContent)
    }
}