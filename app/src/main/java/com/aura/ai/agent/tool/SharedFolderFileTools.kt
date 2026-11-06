package com.aura.ai.agent.tool

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import com.aura.ai.data.settings.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal data class SharedFileEntry(
    val documentId: String,
    val name: String,
    val relativePath: String,
    val mimeType: String?,
)

internal sealed interface SharedFileSearchResult {
    data class Found(val files: List<SharedFileEntry>, val truncated: Boolean) : SharedFileSearchResult
    data object FolderNotSelected : SharedFileSearchResult
    data object FolderPermissionLost : SharedFileSearchResult
    data class InvalidQuery(val reason: String) : SharedFileSearchResult
    data object NoMatches : SharedFileSearchResult
    data class Ambiguous(val files: List<SharedFileEntry>) : SharedFileSearchResult
    data class Failed(val reason: String) : SharedFileSearchResult
}

/** Bounded filename-only search over the SAF folder the user explicitly selected. */
internal class SharedFolderFileIndex(
    private val context: Context,
    private val treeUri: Uri,
) {
    fun search(rawQuery: String): SharedFileSearchResult {
        val query = SharedFileSearchPolicy.query(rawQuery)
            ?: return SharedFileSearchResult.InvalidQuery("Use at least two characters to search filenames.")
        val permission = context.contentResolver.persistedUriPermissions.firstOrNull {
            it.uri == treeUri && it.isReadPermission
        } ?: return SharedFileSearchResult.FolderPermissionLost
        if (!permission.isReadPermission) return SharedFileSearchResult.FolderPermissionLost

        return try {
            val rootId = DocumentsContract.getTreeDocumentId(treeUri)
            val results = mutableListOf<SharedFileEntry>()
            var visited = 0
            var truncated = false

            fun walk(parentId: String, parentPath: String, depth: Int) {
                if (depth > SharedFileSearchPolicy.MAX_DEPTH || visited >= SharedFileSearchPolicy.MAX_VISITED) {
                    truncated = true
                    return
                }
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentId)
                val children = mutableListOf<SharedFileEntry>()
                context.contentResolver.query(
                    childrenUri,
                    arrayOf(
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_MIME_TYPE,
                    ),
                    null,
                    null,
                    "${DocumentsContract.Document.COLUMN_DISPLAY_NAME} ASC",
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val nameColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val mimeColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    while (cursor.moveToNext()) {
                        if (visited >= SharedFileSearchPolicy.MAX_VISITED) {
                            truncated = true
                            break
                        }
                        visited++
                        val id = cursor.getString(idColumn) ?: continue
                        val name = cursor.getString(nameColumn)?.trim().orEmpty()
                        val mime = cursor.getString(mimeColumn)
                        if (name.isBlank() || name.startsWith('.')) continue
                        val path = if (parentPath.isBlank()) name else "$parentPath/$name"
                        children += SharedFileEntry(id, name, path, mime)
                    }
                }
                for (entry in children) {
                    if (entry.mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                        walk(entry.documentId, entry.relativePath, depth + 1)
                    } else if (SharedFileSearchPolicy.matches(entry, query)) {
                        results += entry
                        if (results.size >= SharedFileSearchPolicy.MAX_RESULTS) {
                            truncated = true
                            break
                        }
                    }
                }
            }

            walk(rootId, "", 0)
            val ordered = results.sortedBy { it.relativePath.lowercase() }
            if (ordered.isEmpty()) SharedFileSearchResult.NoMatches
            else SharedFileSearchResult.Found(ordered, truncated)
        } catch (e: SecurityException) {
            SharedFileSearchResult.FolderPermissionLost
        } catch (e: Exception) {
            SharedFileSearchResult.Failed(e.message ?: "Could not search the selected folder")
        }
    }
}

internal object SharedFileSearchPolicy {
    const val MAX_DEPTH = 8
    const val MAX_VISITED = 2_000
    const val MAX_RESULTS = 15

    fun query(raw: String): String? = raw.trim().takeIf {
        it.length in 2..120 && it.none(Char::isISOControl)
    }

    fun matches(entry: SharedFileEntry, query: String): Boolean =
        entry.name.contains(query, ignoreCase = true) || entry.relativePath.contains(query, ignoreCase = true)

    fun selectUnique(entries: List<SharedFileEntry>, query: String): List<SharedFileEntry> {
        val cleaned = query.trim()
        val pathMatches = entries.filter { it.relativePath.equals(cleaned, ignoreCase = true) }
        if (pathMatches.isNotEmpty()) return pathMatches
        val exactNames = entries.filter { it.name.equals(cleaned, ignoreCase = true) }
        if (exactNames.isNotEmpty()) return exactNames
        return entries
    }
}

class SearchSharedFilesTool @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
) : Tool {
    override val name = "search_files"
    override val description = "Search filenames inside the folder the user explicitly granted to Aura. Does not read file contents or modify files."
    override val parameters = mapOf("query" to "At least two characters from a filename or relative path")

    override suspend fun execute(args: Map<String, String>): ToolResult = withContext(Dispatchers.IO) {
        val uriString = settings.settings.first().sharedFilesTreeUri
            ?: return@withContext ToolResult(false, "", "No shared folder is selected. Choose one in Aura Settings under File access.")
        val treeUri = runCatching { Uri.parse(uriString) }.getOrNull()
            ?: return@withContext ToolResult(false, "", "The saved folder reference is invalid. Choose the folder again in Aura Settings.")
        when (val result = SharedFolderFileIndex(context, treeUri).search(args["query"].orEmpty())) {
            is SharedFileSearchResult.Found -> {
                val rows = result.files.joinToString("\n") { "${it.relativePath} (${it.mimeType ?: "unknown type"})" }
                ToolResult(true, rows + if (result.truncated) "\nSearch stopped at its safety limit; refine the query for more." else "")
            }
            SharedFileSearchResult.FolderNotSelected -> ToolResult(false, "", "No shared folder is selected. Choose one in Aura Settings under File access.")
            SharedFileSearchResult.FolderPermissionLost -> ToolResult(false, "", "Aura no longer has access to that folder. Choose it again in Aura Settings under File access.")
            is SharedFileSearchResult.InvalidQuery -> ToolResult(false, "", result.reason)
            SharedFileSearchResult.NoMatches -> ToolResult(true, "No matching filenames were found in the selected folder.")
            is SharedFileSearchResult.Ambiguous -> ToolResult(false, "", "More than one file matched. Use a more specific filename or relative path.")
            is SharedFileSearchResult.Failed -> ToolResult(false, "", "File search failed: ${result.reason}")
        }
    }
}

class OpenSharedFileTool @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
) : Tool {
    override val name = "open_shared_file"
    override val description = "Open one uniquely matched file from the folder the user granted. Aura does not edit or delete files."
    override val parameters = mapOf("filename_or_path" to "Exact filename or relative path within the selected folder")

    override suspend fun execute(args: Map<String, String>): ToolResult = withContext(Dispatchers.IO) {
        val uriString = settings.settings.first().sharedFilesTreeUri
            ?: return@withContext ToolResult(false, "", "No shared folder is selected. Choose one in Aura Settings under File access.")
        val treeUri = Uri.parse(uriString)
        when (val result = SharedFolderFileIndex(context, treeUri).search(args["filename_or_path"].orEmpty())) {
            is SharedFileSearchResult.Found -> {
                val matches = SharedFileSearchPolicy.selectUnique(result.files, args["filename_or_path"].orEmpty())
                if (matches.size != 1) {
                    ToolResult(false, "", "More than one file matched. Use the exact relative path from the search result.")
                } else {
                    val file = matches.single()
                    val fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, file.documentId)
                    val mime = file.mimeType?.takeIf { it.isNotBlank() && it != DocumentsContract.Document.MIME_TYPE_DIR } ?: "*/*"
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(fileUri, mime)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    runCatching {
                        if (intent.resolveActivity(context.packageManager) == null) {
                            ToolResult(false, "", "No installed app can open ${file.name}.")
                        } else {
                            context.startActivity(intent)
                            ToolResult(true, "Opened ${file.relativePath}. Review it in the selected app.")
                        }
                    }.getOrElse { ToolResult(false, "", "Could not open ${file.name}: ${it.message ?: "open failed"}") }
                }
            }
            SharedFileSearchResult.FolderNotSelected -> ToolResult(false, "", "No shared folder is selected. Choose one in Aura Settings under File access.")
            SharedFileSearchResult.FolderPermissionLost -> ToolResult(false, "", "Aura no longer has access to that folder. Choose it again in Aura Settings under File access.")
            is SharedFileSearchResult.InvalidQuery -> ToolResult(false, "", result.reason)
            SharedFileSearchResult.NoMatches -> ToolResult(false, "", "No matching file was found in the selected folder.")
            is SharedFileSearchResult.Ambiguous -> ToolResult(false, "", "More than one file matched. Use an exact relative path.")
            is SharedFileSearchResult.Failed -> ToolResult(false, "", "File search failed: ${result.reason}")
        }
    }
}
