package com.abdulin.nutritionapp.domain.usecase

import com.abdulin.nutritionapp.core.utils.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CheckAuthUseCase @Inject constructor(
    private val tokenManager: TokenManager
) {

    suspend operator fun invoke(): Boolean {
        return tokenManager.getAccessToken() != null
    }
}
