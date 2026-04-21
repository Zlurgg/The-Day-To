package uk.co.zlurgg.thedayto.auth.domain.model

/**
 * Type-safe wrapper for Google ID tokens.
 * Prevents parameter-order bugs at near-zero runtime cost.
 */
@JvmInline
value class IdToken(val value: String)
