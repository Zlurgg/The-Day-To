package uk.co.zlurgg.thedayto.core.domain.util

import uk.co.zlurgg.thedayto.core.domain.util.Error

enum class NetworkError: Error {
    REQUEST_TIMEOUT,
    TOO_MANY_REQUESTS,
    NO_INTERNET,
    SERVER_ERROR,
    SERIALIZATION,
    UNKNOWN,
}