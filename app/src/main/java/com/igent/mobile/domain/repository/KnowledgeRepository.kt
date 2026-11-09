package com.igent.mobile.domain.repository

data class KnowledgeItem(
    val path: String,
    val title: String,
    val category: String,
    val content: String
)

interface KnowledgeRepository {
    fun verifyAssetCount(): Int
    suspend fun getGlossary(): Map<String, String>
    suspend fun listKnowledgeFiles(): List<KnowledgeItem>
    suspend fun searchKnowledge(query: String): List<KnowledgeItem>
}
