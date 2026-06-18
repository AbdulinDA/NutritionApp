package com.abdulin.nutritionapp.core.network

import android.util.Log
import com.abdulin.nutritionapp.BuildConfig
import com.abdulin.nutritionapp.core.utils.Resource
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.Response
import java.net.SocketTimeoutException
import java.util.Locale

private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

private const val TAG = "SafeApiCall"

private fun debugLog(message: String) {
    if (BuildConfig.ENABLE_NETWORK_LOGS) {
        Log.d(TAG, message)
    }
}

private fun errorLog(message: String, throwable: Throwable? = null) {
    if (throwable != null) {
        Log.e(TAG, message, throwable)
    } else {
        Log.e(TAG, message)
    }
}

private fun localizedString(ru: String, en: String): String {
    return if (Locale.getDefault().language == "ru") ru else en
}

private fun timeoutMessage(): String = localizedString(
    "Сервер отвечает слишком долго. Попробуйте еще раз.",
    "The server is taking too long to respond. Please try again."
)

private fun genericNetworkMessage(): String = localizedString(
    "Не удалось выполнить запрос. Проверьте подключение и попробуйте снова.",
    "Could not complete the request. Check your connection and try again."
)

private fun emptyResponseBodyMessage(): String = localizedString(
    "Пустой ответ от сервера",
    "Empty response body"
)

private fun emptyResponseDataMessage(): String = localizedString(
    "В ответе нет данных",
    "Empty response data"
)

private fun unexpectedResponseFormatMessage(): String = localizedString(
    "Неожиданный формат ответа",
    "Unexpected response format"
)

private fun unknownServerErrorMessage(): String = localizedString(
    "Неизвестная ошибка сервера",
    "Unknown server error"
)

private fun httpErrorMessage(code: Int): String = localizedString(
    "Ошибка HTTP $code",
    "HTTP error $code"
)

private fun parseErrorMessage(errorJson: String?): String? {
    if (errorJson.isNullOrBlank()) return null

    return try {
        val root = json.parseToJsonElement(errorJson).jsonObject
        val errorMessage = root["error"]
            ?.jsonObject
            ?.get("message")
            ?.jsonPrimitive
            ?.contentOrNull
        val message = root["message"]?.jsonPrimitive?.contentOrNull
        errorMessage ?: message
    } catch (_: Exception) {
        null
    }
}

internal suspend inline fun <reified T> safeApiCall(
    apiCall: suspend () -> Response<ApiResponse<T>>
): Resource<T> {
    return try {
        val response = apiCall()
        val url = response.raw().request.url
        val code = response.code()

        debugLog("Request: $url")

        if (code == 204) {
            @Suppress("UNCHECKED_CAST")
            return if (T::class == Unit::class) {
                Resource.Success(Unit as T)
            } else {
                Resource.Error(emptyResponseBodyMessage())
            }
        }

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                if (body.success) {
                    val data = body.data
                    debugLog("Success [$url]: ${T::class.simpleName ?: "response"}")
                    @Suppress("UNCHECKED_CAST")
                    val normalizedData = when {
                        data is T -> data
                        T::class == Unit::class -> Unit as T
                        data == null -> return Resource.Error(emptyResponseDataMessage())
                        else -> return Resource.Error(unexpectedResponseFormatMessage())
                    }
                    Resource.Success(normalizedData)
                } else {
                    val errorMsg = body.error?.message ?: body.message ?: unknownServerErrorMessage()
                    errorLog("Server Error [$url]: $errorMsg")
                    Resource.Error(errorMsg)
                }
            } else {
                errorLog("Response body is null [$url]")
                Resource.Error(emptyResponseBodyMessage())
            }
        } else {
            val errorJson = response.errorBody()?.string()
            val errorMessage = parseErrorMessage(errorJson)
            errorLog("HTTP Error [$url] $code: ${errorMessage ?: "unparsed error"}")
            Resource.Error(errorMessage ?: httpErrorMessage(code))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: SocketTimeoutException) {
        errorLog("Request timed out")
        Resource.Error(timeoutMessage())
    } catch (e: Exception) {
        errorLog("Network Exception", e)
        Resource.Error(e.message ?: genericNetworkMessage())
    }
}
