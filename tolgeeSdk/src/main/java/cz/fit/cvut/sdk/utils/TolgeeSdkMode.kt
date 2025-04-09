package cz.fit.cvut.sdk.utils

/**
 * Mode in which the SDK operates
 */
enum class TolgeeSdkMode {
    DEBUG,      // For development
    RELEASE,    // Production mode with preloaded translations
    HYBRID      // Could be used for specific scenarios in the future
}