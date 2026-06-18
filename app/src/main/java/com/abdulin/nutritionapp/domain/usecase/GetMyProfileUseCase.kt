package com.abdulin.nutritionapp.domain.usecase

import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.auth.UserResponseDto
import com.abdulin.nutritionapp.domain.repository.UserRepository
import javax.inject.Inject

class GetMyProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Resource<UserResponseDto> {
        return repository.getMyProfile()
    }
}
