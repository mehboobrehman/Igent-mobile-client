package com.igent.mobile.data.repository

import android.content.Context
import com.igent.mobile.domain.repository.KnowledgeItem
import com.igent.mobile.domain.repository.KnowledgeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KnowledgeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : KnowledgeRepository {

    private val assetManager = context.assets
    private var cachedFiles: List<KnowledgeItem>? = null
    private var cachedGlossary: Map<String, String>? = null

    override fun verifyAssetCount(): Int {
        return countAssetsRecursive("knowledge")
    }

    private fun countAssetsRecursive(path: String): Int {
        var count = 0
        try {
            val list = assetManager.list(path) ?: return 0
            if (list.isEmpty()) {
                return 1
            }
            for (item in list) {
                val subPath = "$path/$item"
                count += countAssetsRecursive(subPath)
            }
        } catch (e: Exception) {
            return 1
        }
        return count
    }

    override suspend fun getGlossary(): Map<String, String> = withContext(Dispatchers.IO) {
        cachedGlossary?.let { return@withContext it }
        val glossaryMap = mutableMapOf<String, String>()
        try {
            val inputStream = assetManager.open("knowledge/glossary.json")
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val jsonString = reader.readText()
            reader.close()

            val jsonObject = Json.parseToJsonElement(jsonString) as? JsonObject
            jsonObject?.forEach { (key, element) ->
                glossaryMap[key] = element.jsonPrimitive.content
            }
        } catch (e: Exception) {
            // fallback
        }
        cachedGlossary = glossaryMap
        glossaryMap
    }

    override suspend fun listKnowledgeFiles(): List<KnowledgeItem> = withContext(Dispatchers.IO) {
        cachedFiles?.let { return@withContext it }
        val items = mutableListOf<KnowledgeItem>()
        loadMarkdownFilesRecursive("knowledge", items)
        cachedFiles = items
        items
    }

    private fun loadMarkdownFilesRecursive(path: String, items: MutableList<KnowledgeItem>) {
        try {
            val list = assetManager.list(path) ?: return
            for (item in list) {
                val subPath = "$path/$item"
                if (item.endsWith(".md")) {
                    try {
                        val inputStream = assetManager.open(subPath)
                        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                        val content = reader.readText()
                        reader.close()

                        val titleLine = content.lineSequence().find { it.startsWith("# ") }
                        val title = titleLine?.removePrefix("# ")?.trim() ?: item.removeSuffix(".md")
                        val category = path.substringAfter("knowledge/").ifEmpty { "general" }

                        items.add(
                            KnowledgeItem(
                                path = subPath,
                                title = title,
                                category = category,
                                content = content
                            )
                        )
                    } catch (e: Exception) {
                        // skip
                    }
                } else if (!item.contains(".")) {
                    loadMarkdownFilesRecursive(subPath, items)
                }
            }
        } catch (e: Exception) {
            // skip
        }
    }

    override suspend fun searchKnowledge(query: String): List<KnowledgeItem> = withContext(Dispatchers.IO) {
        val files = listKnowledgeFiles()
        val lowerQuery = query.lowercase()
        files.filter {
            it.title.lowercase().contains(lowerQuery) ||
            it.content.lowercase().contains(lowerQuery) ||
            it.category.lowercase().contains(lowerQuery)
        }
    }
}
