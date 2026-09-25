package com.aura.ai.coding.domain.repository

import com.aura.ai.coding.domain.BuildResult
import com.aura.ai.coding.domain.RepositoryInfo
import com.aura.ai.coding.domain.TestResult

interface ICodingRepository {
    suspend fun inspectRepository(owner: String, repo: String): Result<RepositoryInfo>
    suspend fun readFile(owner: String, repo: String, path: String, branch: String = "main"): Result<String>
    suspend fun createFile(owner: String, repo: String, path: String, content: String, message: String, branch: String = "main"): Result<Boolean>
    suspend fun modifyFile(owner: String, repo: String, path: String, content: String, message: String, branch: String = "main"): Result<Boolean>
    suspend fun deleteFile(owner: String, repo: String, path: String, message: String, branch: String = "main"): Result<Boolean>
    suspend fun getGitStatus(owner: String, repo: String): Result<String>
    suspend fun getGitDiff(owner: String, repo: String, ref1: String, ref2: String): Result<String>
    suspend fun buildProject(owner: String, repo: String): Result<BuildResult>
    suspend fun runTests(owner: String, repo: String): Result<TestResult>
}
