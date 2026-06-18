package com.abdulin.nutritionapp.domain.usecase

import android.provider.Settings
import android.content.Context
import com.abdulin.nutritionapp.data.remote.NutritionApi
import com.abdulin.nutritionapp.data.remote.PushTokenRequestDto
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UpdatePushTokenUseCase @Inject constructor(
    private val api: NutritionApi,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(token: String) {
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        try {
            api.updatePushToken(
                PushTokenRequestDto(
                    token = token,
                    deviceId = deviceId
                )
            )
        } catch (e: Exception) {
            // Ошибка синхронизации токена не должна блокировать работу
        }
    }
}
