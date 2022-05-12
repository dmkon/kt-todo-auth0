package com.nortohol.auth0

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.nortohol.auth0.util.UiText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class TodoAuthViewModel(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
): ViewModel() {
    private val TAG = "ViewModel"
    private val _uiState = MutableStateFlow<ToDoUiState>(ToDoUiState.Empty(data = TodoAuthUiModel()))
    val uiState: StateFlow<ToDoUiState> = _uiState

    var isSignedIn: Boolean = false
    var accessToken: String = ""
    var email: String = ""
    var name: String = ""

    private val account: Auth0 = Auth0(
        clientId = "cSMIplZenVQndN60QeT0cGkDFPmn8GVP",
        domain = "dev-8co-5m1c.us.auth0.com"
    )
    private val schema = "demo"

    init {
        Log.d(TAG, "init")
    }

    fun logIn(context: Context) {
        _uiState.value = ToDoUiState.Loading

        viewModelScope.launch(dispatcher) {
            try {
                if (Random().nextBoolean()) {
                    throw Exception("Random Test Exception")
                }
                WebAuthProvider.login(account)
                    .withScheme(schema)
                    .withScope("openid profile email")
                    .start(
                        context,
                        object : Callback<Credentials, AuthenticationException> {
                            override fun onSuccess(result: Credentials) {
                                Log.d(
                                    TAG,
                                    "logIn token: ${result.accessToken}, Id: ${result.idToken}"
                                )
                                this@TodoAuthViewModel.isSignedIn = true
                                this@TodoAuthViewModel.accessToken = result.accessToken
                                _uiState.value = ToDoUiState.Loaded(
                                    data = TodoAuthUiModel(
                                        isSignedIn = true,
                                        name = this@TodoAuthViewModel.name,
                                        email = this@TodoAuthViewModel.email
                                    )
                                )
                            }

                            override fun onFailure(error: AuthenticationException) {
                                val message = "${error.getCode()} - ${error.getDescription()}"
                                Log.e(TAG, message)
                                _uiState.value = ToDoUiState.Error(
                                    data = TodoAuthUiModel(
                                        isSignedIn = this@TodoAuthViewModel.isSignedIn,
                                        name = this@TodoAuthViewModel.name,
                                        email = this@TodoAuthViewModel.email
                                    ),
                                    message = UiText.DynamicString(message)
                                )
                            }
                        }
                    )
            } catch (ex: Exception) {
                val message = ex.localizedMessage ?: "WebAuthProvider logIn error"
                Log.e(TAG, message)
                _uiState.value = ToDoUiState.Error(
                    data = TodoAuthUiModel(
                        isSignedIn = this@TodoAuthViewModel.isSignedIn,
                        name = this@TodoAuthViewModel.name,
                        email = this@TodoAuthViewModel.email
                    ),
                    message = UiText.DynamicString(message)
                )
            }
        }
    }

    fun getProfile() {
        _uiState.value = ToDoUiState.Loading
        viewModelScope.launch(dispatcher) {
            try {
                val client = AuthenticationAPIClient(account)
                client.userInfo(accessToken)
                    .start(object : Callback<UserProfile, AuthenticationException> {
                        override fun onFailure(ex: AuthenticationException) {
                            // Something went wrong!
                            val message = ex.localizedMessage.let { it } ?: "WebAuthProvider getProfile error"
                            Log.e(TAG, message)
                            _uiState.value = ToDoUiState.Error(
                                data = TodoAuthUiModel(
                                    isSignedIn = this@TodoAuthViewModel.isSignedIn,
                                    name = this@TodoAuthViewModel.name,
                                    email = this@TodoAuthViewModel.email
                                ),
                                message = UiText.DynamicString(message)
                            )
                        }

                        override fun onSuccess(profile: UserProfile) {
                            // We have the user's profile!
                            val email: String = if (!profile.email.isNullOrEmpty()) {
                                profile.email.toString()
                            } else {
                                ""
                            }
                            val name: String = if (!profile.name.isNullOrEmpty()) {
                                profile.name.toString()
                            } else {
                                ""
                            }
                            this@TodoAuthViewModel.email = email
                            this@TodoAuthViewModel.name = name
                            _uiState.value = ToDoUiState.Loaded(
                                data = TodoAuthUiModel(
                                        isSignedIn = true,
                                        email = email,
                                        name = name
                                    )
                            )
                        }
                    })
            } catch (ex: Exception) {
                val message = ex.localizedMessage ?: "WebAuthProvider getProfile error"
                Log.e(TAG, message)
                _uiState.value = ToDoUiState.Error(
                    data = TodoAuthUiModel(
                        isSignedIn = this@TodoAuthViewModel.isSignedIn,
                        name = this@TodoAuthViewModel.name,
                        email = this@TodoAuthViewModel.email
                    ),
                    message = UiText.DynamicString(message)
                )
            }
        }
    }

    fun logOut(context: Context) {
        _uiState.value = ToDoUiState.Loading
        viewModelScope.launch(dispatcher) {
            try {
                WebAuthProvider.logout(account)
                    .withScheme(schema)
                    .start(context, object: Callback<Void?, AuthenticationException> {
                        override fun onSuccess(payload: Void?) {
                            // The user has been logged out!
                            this@TodoAuthViewModel.isSignedIn = false
                            this@TodoAuthViewModel.name = ""
                            this@TodoAuthViewModel.email = ""
                            this@TodoAuthViewModel.accessToken = ""
                            _uiState.value = ToDoUiState.Loaded(
                                data = TodoAuthUiModel(
                                    isSignedIn = false,
                                    name = "",
                                    email = ""
                                )
                            )
                        }

                        override fun onFailure(ex: AuthenticationException) {
                            // Something went wrong!
                            val message = "${ex.getCode()} - ${ex.getDescription()}"
                            Log.e(TAG, message)
                            _uiState.value = ToDoUiState.Error(
                                data = TodoAuthUiModel(
                                    isSignedIn = this@TodoAuthViewModel.isSignedIn,
                                    name = this@TodoAuthViewModel.name,
                                    email = this@TodoAuthViewModel.email
                                ),
                                message = UiText.DynamicString(message)
                            )
                        }
                    })
            } catch (ex: Exception) {
                val message = ex.localizedMessage ?: "WebAuthProvider logOut error"
                Log.e(TAG, message)
                _uiState.value = ToDoUiState.Error(
                    data = TodoAuthUiModel(
                        isSignedIn = this@TodoAuthViewModel.isSignedIn,
                        name = this@TodoAuthViewModel.name,
                        email = this@TodoAuthViewModel.email
                    ),
                    message = UiText.DynamicString(message)
                )
            }
        }
    }

    sealed class ToDoUiState {
        class Empty(val data: TodoAuthUiModel) : ToDoUiState()
        object Loading : ToDoUiState()
        class Loaded(val data: TodoAuthUiModel) : ToDoUiState()
        class Error(
            val data: TodoAuthUiModel,
            val message: UiText
        ) : ToDoUiState()
    }
}
