package com.nortohol.auth0.util

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RequestError(
    @Json(name = "error") val error: String,
    @Json(name = "error_description") val description: String
)