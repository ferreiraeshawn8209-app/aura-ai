package com.aura.ai.coding.domain

import kotlinx.serialization.Serializable

@Serializable
data class RepositoryInfo(
    val owner: String,
    val name: String,
    val url: String,
    val defaultBranch: String,
    val description: String? = null
)

@Serializable
data class FileOperation(
    val path: String,
    val operation: String, // CREATE, MODIFY, DELETE, READ
    val content: String? = null,
    val branch: String = "main",
    val commitMessage: String? = null
)

@Serializable
data class BuildResult(
    val success: Boolean,
    val output: String,
    val errors: List<String>,
    val warnings: List<String>,
    val durationMs: Long
)

@Serializable
data class TestResult(
    val success: Boolean,
    val totalTests: Int,
    val passedTests: Int,
    val failedTests: Int,
    val skippedTests: Int,
    val output: String,
    val durationMs: Long
)
