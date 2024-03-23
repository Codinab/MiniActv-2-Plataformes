package com.example.locationupdateskotlin.util

/**
 * A wrapper class for LiveData events that are intended to be consumed only once.
 * This is useful for events like navigation or Snackbar messages, where you don't want
 * a configuration change (like rotation) to trigger the event again.
 *
 * @param T The type of the content being held by the event.
 * @property content The actual content of the event.
 */
open class Event<out T>(private val content: T) {
    // Indicates whether the event has already been handled.
    var hasBeenHandled = false
        private set // External read is allowed but not set.

    /**
     * Returns the content and prevents its use again if it hasn't been handled already.
     * If the event has been handled, returns null.
     *
     * @return The content if it hasn't been handled, or null if it has.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     *
     * @return The content of the event.
     */
    fun peekContent(): T = content
}
