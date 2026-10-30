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
        val todayStr = dateFormat.format(Date())

        // Generate dynamic cards based on live PSX data if available
        val stocks = psxRepository.getStocks().getOrDefault(emptyList())
        val topStock = stocks.maxByOrNull { it.percentChange }

        val newCards = mutableListOf<IdeaCardEntity>()

        if (topStock != null) {
            newCards.add(
                IdeaCardEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Daily Momentum Pick: ${topStock.symbol}",
                    summary = "${topStock.name} gained +${topStock.percentChange}% today trading at Rs. ${topStock.price}. Strong volume indicates continued bullish momentum.",
                    recommendation = "ACCUMULATE",
                    category = "STOCK",
                    targetSymbol = topStock.symbol,
                    dateString = todayStr
                )
            )
        } else {
            newCards.add(
                IdeaCardEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Blue-Chip Focus: LUCK Cement",
                    summary = "Lucky Cement maintains strong export volume and solid balance sheet despite construction sector cycles.",
                    recommendation = "BUY",
                    category = "STOCK",
                    targetSymbol = "LUCK",
                    dateString = todayStr
                )
            )
        }

        newCards.add(
            IdeaCardEntity(
                id = UUID.randomUUID().toString(),
                title = "Islamic Mutual Fund Pick: Meezan Islamic Fund",
                summary = "Top-performing Shariah-compliant equity fund targeting high-capitalization PSX dividend stocks.",
                recommendation = "STRONG BUY",
                category = "FUND",
                targetSymbol = "MEEZAN",
                dateString = todayStr
            )
        )

        newCards.add(
            IdeaCardEntity(
                id = UUID.randomUUID().toString(),
                title = "Macro Insight: SBP Policy Rate & Dividend Yields",
                summary = "Easing policy rates create favorable tailwinds for high dividend payout stocks in Pakistan Commercial Banking & Energy sectors.",
                recommendation = "HOLD / OVERWEIGHT",
                category = "MACRO",
                targetSymbol = "PSX",
                dateString = todayStr
            )
        )

        ideaCardDao.insertIdeaCards(newCards)
        ideaCardDao.getAllIdeaCardsOnce()
    }

    override suspend fun initializeDefaultIdeaCards(): Unit = withContext(Dispatchers.IO) {
        val existing = ideaCardDao.getAllIdeaCardsOnce()
        if (existing.isEmpty()) {
            generateDailyIdeaCards()
        }
    }
}
