package com.nortohol.auth0

import com.nortohol.auth0.util.JsonApiError
import com.nortohol.auth0.util.RequestError
import org.json.JSONObject

/**
 * Defines the possible outcomes of a request.
 * Success, with the requested data, or Failure, with an error response.
 */
sealed class Outcome<out T> {
    fun fold(onSuccess: (T) -> Unit, onFailure: (String) -> Unit) {
        when (this) {
            is Success -> onSuccess(response)
            is Failure -> onFailure(errorResponse)
        }
    }
}

/**
 * Called when [requestCallback] makes a request successfully
 *
 * @property response Object requested from backend
 */
open class Success<out T>(val response: T) : Outcome<T>() {
    operator fun invoke(): T = response

    override fun equals(other: Any?): Boolean {
        return (other as? Success<*>)?.response?.equals(this.response) == true
    }

    override fun hashCode(): Int {
        return response?.hashCode() ?: 0
    }
}

/**
 * Used by the repository to cache API responses
 */
class ApiSuccess<out T>(
    response: T,
    val jsonResponse: JSONObject
) : Success<T>(response)

/**
 * Called when [requestCallback] fails to make a request
 *
 * @property errorResponse The error message returned
 * @property errorCode The HTTP error code
 * @property errorResponse The deserialized body of the socket error response if it exists
 * @property jsonApiErrors The deserialized json:api rest body of the error response if it exists
 * TODO: we should find a way to separate out jsonApi failures and socket failures, it likely
 * will require us to create a new Outcome object to do it cleanly.
 */
data class Failure(
    val errorResponse: String,
    val errorCode: Int = -1,
    val requestError: RequestError? = null,
    val jsonApiErrors: List<JsonApiError> = listOf()
) : Outcome<Nothing>()

/**
 * Defines Callback lambda
 */
typealias Callback<T> = (result: Outcome<T>) -> Unit