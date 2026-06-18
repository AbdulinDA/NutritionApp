package com.abdulin.nutritionapp.domain.usecase

import android.content.Context
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.core.utils.Resource
import com.abdulin.nutritionapp.data.dto.user.WaterLogRequestDto
import com.abdulin.nutritionapp.data.health.HealthConnectManager
import com.abdulin.nutritionapp.data.remote.NutritionApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LogWaterUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: NutritionApi,
    private val healthConnectManager: HealthConnectManager
) {
    suspend operator fun invoke(amountMl: Int, date: String): Resource<Unit> {
        val response = try {
            val res = api.logWater(WaterLogRequestDto(amountMl, date))
            if (res.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(res.message() ?: context.getString(R.string.log_water_server_error))
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: context.getString(R.string.log_water_network_error))
        }

        if (response is Resource.Success) {
            healthConnectManager.writeWater(amountMl)
        }

        return response
    }
}
