package com.nortohol.auth0

data class TodoAuthUiModel(
    val isSignedIn: Boolean = false,
    val email: String = "",
    val name: String = ""
)