package com.nortohol.auth0.util

import com.squareup.moshi.JsonClass

/**
 * Generic data class for JSON:API errors.
 *
 * If you want a strongly typed [meta] object, you can
 * make a custom error class for your specific endpoint
 * that would replace usage of this class.
 */
@JsonClass(generateAdapter = true)
data class JsonApiError(
    val id: String = "",
    val status: Int = 0,
    val title: String = "",
    val code: String = "",
    val detail: String = "",
    val meta: Map<String, Any> = emptyMap()
)