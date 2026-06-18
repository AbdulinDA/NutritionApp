package com.abdulin.nutritionapp.presentation.auth

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: (String) -> Unit,
    onBackToLogin: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val trimmedEmail = email.trim()
    val isEmailValid = trimmedEmail.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()
    val isPasswordValid = password.length >= 8
    val isFormValid = isEmailValid && isPasswordValid

    LaunchedEffect(state) {
        val successState = state as? Resource.Success
        val destination = successState?.data
        if (!destination.isNullOrBlank()) {
            onRegisterSuccess(destination)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.register_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.onboarding_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val contentWidth = if (maxWidth > 600.dp) 0.5f else 1f

            Column(
                modifier = Modifier
                    .fillMaxWidth(contentWidth)
                    .padding(16.dp)
                    .align(Alignment.Center)
            ) {
            Text(
                text = stringResource(R.string.register_compact_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (state is Resource.Error) {
                Text(
                    text = (state as Resource.Error).message ?: stringResource(R.string.register_error_generic),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("${stringResource(R.string.onboarding_email_hint)} *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = email.isNotBlank() && !isEmailValid,
                supportingText = {
                    Text(
                        if (email.isNotBlank() && !isEmailValid) {
                            stringResource(R.string.register_error_email)
                        } else {
                            stringResource(R.string.register_email_help)
                        }
                    )
                }
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("${stringResource(R.string.onboarding_pass_hint)} *") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = password.isNotEmpty() && !isPasswordValid,
                supportingText = {
                    Text(
                        if (password.isNotEmpty() && !isPasswordValid) {
                            stringResource(R.string.register_error_password)
                        } else {
                            stringResource(R.string.register_password_help)
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.register(
                        email = trimmedEmail,
                        password = password
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is Resource.Loading && isFormValid
            ) {
                if (state is Resource.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(stringResource(R.string.register_submit))
                }
            }

            TextButton(
                onClick = onBackToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.register_back_to_login))
            }

            Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
