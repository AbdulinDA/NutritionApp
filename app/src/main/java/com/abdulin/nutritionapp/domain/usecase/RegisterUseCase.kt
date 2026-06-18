package com.abdulin.nutritionapp.domain.usecase

import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.auth.RegisterRequestDto
import com.abdulin.nutritionapp.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(request: RegisterRequestDto): Resource<String> {
        val registerResult = repository.register(request)

        return when (registerResult) {
            is Resource.Success -> Resource.Success(
                registerResult.data?.message ?: "Registration successful"
            )
            is Resource.Error -> Resource.Error(registerResult.message ?: "Registration failed")
            is Resource.Loading -> Resource.Loading()
        }
    }
}
