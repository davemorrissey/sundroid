package uk.co.sundroid.activity

enum class LocaterStatus {

    /**
     * Locater is running. Location or error will be sent later.
     */
    STARTED,

    /**
     * Location permission is denied.
     */
    DENIED,

    /**
     * The fused location provider is disabled.
     */
    DISABLED,

    /**
     * No location or other error received within timeout period.
     */
    TIMEOUT,

    /**
     * No location service or provider is available.
     */
    UNAVAILABLE,

    /**
     * Unable to start at the moment. Used by widgets only.
     */
    BLOCKED

}