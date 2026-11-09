package com.igent.mobile.data.repository

import com.igent.mobile.data.local.dao.IdeaCardDao
import com.igent.mobile.data.local.entity.IdeaCardEntity
import com.igent.mobile.domain.repository.IdeaCardRepository
import com.igent.mobile.domain.repository.PsxRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdeaCardRepositoryImpl @Inject constructor(
    private val ideaCardDao: IdeaCardDao,
    private val psxRepository: PsxRepository
) : IdeaCardRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun getAllIdeaCards(): Flow<List<IdeaCardEntity>> {
        return ideaCardDao.getAllIdeaCards()
    }

    override suspend fun getIdeaCardForDate(dateString: String): IdeaCardEntity? = withContext(Dispatchers.IO) {
        ideaCardDao.getIdeaCardForDate(dateString)
    }

    override suspend fun generateDailyIdeaCards(): List<IdeaCardEntity> = withContext(Dispatchers.IO) {
        // Return empty list until the real card engine (F-CARDS) is fully integrated
        emptyList<IdeaCardEntity>()
    }

    override suspend fun initializeDefaultIdeaCards(): Unit = withContext(Dispatchers.IO) {
        // Disabled until real card engine (F-CARDS) is ready
    }
}
