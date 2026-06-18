package com.abdulin.nutritionapp.presentation.auth

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val nextDestination: String? = null,
    val error: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)
