package com.abdulin.nutritionapp.domain.usecase

import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(
        email: String,
        password: String
    ): Resource<Unit> {
        return repository.login(email, password)
    }
}
