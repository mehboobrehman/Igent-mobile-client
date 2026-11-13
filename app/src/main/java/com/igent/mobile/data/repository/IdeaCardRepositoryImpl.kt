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
        initializeDefaultIdeaCards()
        ideaCardDao.getAllIdeaCardsOnce()
    }

    override suspend fun initializeDefaultIdeaCards(): Unit = withContext(Dispatchers.IO) {
        if (ideaCardDao.getAllIdeaCardsOnce().isEmpty()) {
            val todayStr = dateFormat.format(Date())
            val cards = listOf(
                IdeaCardEntity(
                    id = "idea_1",
                    title = "Lucky Cement (LUCK) - Dividend & Export Accumulation",
                    summary = "Lucky Cement shows robust cash generation, strong export volumes to South Africa/North, and solid free cash flow.",
                    recommendation = "ACCUMULATE on dips below Rs 740. Target: Rs 820. Stop-loss: Rs 710.",
                    category = "STOCK",
                    targetSymbol = "LUCK",
                    dateString = todayStr,
                    timestamp = System.currentTimeMillis()
                ),
                IdeaCardEntity(
                    id = "idea_2",
                    title = "Engro Corporation (ENGRO) - Value & Diversification Play",
                    summary = "Engro offers diversified earnings across fertilizers, connectivity, energy, and dairy with high dividend yield.",
                    recommendation = "BUY for medium-term holding. Dividend yield projected > 9%.",
                    category = "STOCK",
                    targetSymbol = "ENGRO",
                    dateString = todayStr,
                    timestamp = System.currentTimeMillis() - 86400000L
                ),
                IdeaCardEntity(
                    id = "idea_3",
                    title = "Oil & Gas Development Co. (OGDC) - High Yield Energy Hedge",
                    summary = "Sovereign-backed OGDC provides stellar dividend payouts and acts as an effective hedge against inflation and currency depreciation.",
                    recommendation = "HOLD / BUY. High cash reserves and steady production profile.",
                    category = "STOCK",
                    targetSymbol = "OGDC",
                    dateString = todayStr,
                    timestamp = System.currentTimeMillis() - 172800000L
                ),
                IdeaCardEntity(
                    id = "idea_4",
                    title = "MCB Bank Limited (MCB) - Tier-1 Banking Resilience",
                    summary = "Stellar capital adequacy ratio, low infection ratio, and consistent quarterly dividend distribution make MCB a core banking holding.",
                    recommendation = "ACCUMULATE for long-term compounding.",
                    category = "STOCK",
                    targetSymbol = "MCB",
                    dateString = todayStr,
                    timestamp = System.currentTimeMillis() - 259200000L
                )
            )
            ideaCardDao.insertIdeaCards(cards)
        }
    }
}
