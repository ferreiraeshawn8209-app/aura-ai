package com.aura.ai.data.remote.core

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CoreService {
    @GET("api/master")
    suspend fun getMasterState(): Response<CoreMasterResponse>

    @POST("api/master")
    suspend fun setSafeMode(@Body request: SafeModeRequest): Response<SafeModeResponse>
}
