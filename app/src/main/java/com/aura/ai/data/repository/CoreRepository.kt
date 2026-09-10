package com.aura.ai.data.repository

import com.aura.ai.data.remote.core.CoreMasterResponse
import com.aura.ai.data.remote.core.CoreResult
import com.aura.ai.data.remote.core.CoreService
import com.aura.ai.data.remote.core.SafeModeRequest
import com.aura.ai.data.remote.core.SafeModeResponse
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoreRepository @Inject constructor(
    private val service: CoreService
) {
    suspend fun getMasterState(): CoreResult<CoreMasterResponse> = request { service.getMasterState() }

    suspend fun setSafeMode(enabled: Boolean): CoreResult<SafeModeResponse> =
        request { service.setSafeMode(SafeModeRequest(enabled = enabled)) }

    private suspend fun <T> request(call: suspend () -> retrofit2.Response<T>): CoreResult<T> =
        withContext(Dispatchers.IO) {
            try {
                val response = call()
                when {
                    response.isSuccessful && response.body() != null -> CoreResult.Success(response.body()!!)
                    response.code() == 401 -> CoreResult.Unauthenticated
                    response.code() == 403 -> CoreResult.Forbidden
                    else -> CoreResult.ServerError(response.code(), response.errorBody()?.string())
                }
            } catch (error: SocketTimeoutException) {
                CoreResult.Timeout
            } catch (error: IOException) {
                CoreResult.NetworkUnavailable
            } catch (error: HttpException) {
                CoreResult.ServerError(error.code(), error.message())
            } catch (error: Throwable) {
                CoreResult.Unexpected(error)
            }
        }
}
