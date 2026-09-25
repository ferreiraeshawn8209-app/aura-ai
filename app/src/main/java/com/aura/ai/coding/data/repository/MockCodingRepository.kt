package com.aura.ai.coding.data.repository

import com.aura.ai.coding.domain.BuildResult
import com.aura.ai.coding.domain.RepositoryInfo
import com.aura.ai.coding.domain.TestResult
import com.aura.ai.coding.domain.repository.ICodingRepository
import javax.inject.Inject

class MockCodingRepository @Inject constructor() : ICodingRepository {
    override suspend fun inspectRepository(owner: String, repo: String): Result<RepositoryInfo> {
        return Result.failure(Exception("Real GitHub integration required"))
    }

    override suspend fun readFile(owner: String, repo: String, path: String, branch: String): Result<String> {
        return Result.failure(Exception("Real GitHub integration required"))
    }

    override suspend fun createFile(owner: String, repo: String, path: String, content: String, message: String, branch: String): Result<Boolean> {
        return Result.failure(Exception("File operations require GitHub authentication and API access"))
    }

    override suspend fun modifyFile(owner: String, repo: String, path: String, content: String, message: String, branch: String): Result<Boolean> {
        return Result.failure(Exception("File operations require GitHub authentication and API access"))
    }

    override suspend fun deleteFile(owner: String, repo: String, path: String, message: String, branch: String): Result<Boolean> {
        return Result.failure(Exception("File deletion is destructive - requires GitHub authentication and explicit confirmation"))
    }

    override suspend fun getGitStatus(owner: String, repo: String): Result<String> {
        return Result.failure(Exception("Real GitHub integration required"))
    }

    override suspend fun getGitDiff(owner: String, repo: String, ref1: String, ref2: String): Result<String> {
        return Result.failure(Exception("Real GitHub integration required"))
    }

    override suspend fun buildProject(owner: String, repo: String): Result<BuildResult> {
        return Result.failure(Exception("Build execution requires local environment setup"))
    }

    override suspend fun runTests(owner: String, repo: String): Result<TestResult> {
        return Result.failure(Exception("Test execution requires local environment setup"))
    }
}
