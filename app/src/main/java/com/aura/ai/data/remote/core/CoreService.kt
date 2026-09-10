package com.aura.ai.data.remote.core

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface CoreService {
    @GET("api/master")
    suspend fun getMasterState(): Response<CoreMasterResponse>

    @POST("api/master")
    suspend fun setSafeMode(@Body request: SafeModeRequest): Response<SafeModeResponse>

    @GET("api/tasks")
    suspend fun listTasks(
        @Query("user") user: String? = null,
        @Query("project") project: String? = null,
        @Query("status") status: String? = null,
        @Query("limit") limit: Int? = null
    ): Response<List<CoreTask>>

    @POST("api/tasks")
    suspend fun createTask(@Body request: CreateTaskRequest): Response<CoreTask>
}
