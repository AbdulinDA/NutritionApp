package com.abdulin.nutritionapp.data.repository

import android.content.Context
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.network.safeApiCall
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.home.HomeResponseDto
import com.abdulin.nutritionapp.data.mapper.toDomain
import com.abdulin.nutritionapp.data.remote.NutritionApi
import com.abdulin.nutritionapp.domain.model.HomeModel
import com.abdulin.nutritionapp.domain.repository.HomeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val api: NutritionApi,
    @ApplicationContext private val context: Context
) : HomeRepository {
    override suspend fun getHomeData(): Resource<HomeModel> {
        // Explicitly specifying HomeResponseDto helps the compiler resolve the ApiResponse wrapper
        val result = safeApiCall<HomeResponseDto> {
            api.getHomeData()
        }

        return when (result) {
            is Resource.Success -> {
                Resource.Success(result.data!!.toDomain())
            }
            is Resource.Error -> {
                Resource.Error(result.message ?: context.getString(R.string.error_unknown))
            }
            is Resource.Loading -> {
                Resource.Loading()
            }
        }
    }
}
