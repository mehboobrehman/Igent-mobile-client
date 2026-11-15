package com.igent.mobile.ui.dashboard

import com.igent.mobile.data.local.entity.IdeaCardEntity
import com.igent.mobile.domain.model.FeedItem
import com.igent.mobile.domain.model.PortfolioHolding
import com.igent.mobile.domain.model.PortfolioTransaction
import com.igent.mobile.domain.model.Stock
import com.igent.mobile.domain.repository.IdeaCardRepository
import com.igent.mobile.domain.repository.PortfolioRepository
import com.igent.mobile.domain.repository.PsxRepository
import com.igent.mobile.domain.repository.RssRepository
import com.igent.mobile.domain.usecase.GetFeedItemsUseCase
import com.igent.mobile.domain.usecase.GetPortfolioUseCase
import com.igent.mobile.domain.usecase.GetStocksUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testDashboardViewModelLoadsDataSuccessfully() = runTest {
        val fakeStocks = listOf(
            Stock("LUCK", "Lucky Cement", BigDecimal("750.0"), BigDecimal("10.0"), BigDecimal("1.3"), BigDecimal("760.0"), BigDecimal("740.0"), 1000L)
        )
        val fakeFeeds = listOf(
            FeedItem("1", "Title 1", "Desc 1", "link", "date", "source")
        )
        val fakeHoldings = listOf(
            PortfolioHolding("LUCK", "Lucky Cement", BigDecimal("10"), BigDecimal("700"), BigDecimal("750"), BigDecimal("7500"), BigDecimal("500"), BigDecimal("7.1"))
        )

        val psxRepo = object : PsxRepository {
            override suspend fun getStocksState() = com.igent.mobile.domain.model.MarketDataState.Success(fakeStocks)
            override suspend fun getStocks() = Result.success(fakeStocks)
            override suspend fun getStockDetail(symbol: String) = Result.success(fakeStocks[0])
            override fun getStockTickerFlow() = flowOf(fakeStocks)
        }
        val rssRepo = object : RssRepository {
            override suspend fun getFeedItems() = Result.success(fakeFeeds)
            override suspend fun refreshFeed() = Result.success(Unit)
        }
        val portfolioRepo = object : PortfolioRepository {
            override fun getHoldings() = flowOf(fakeHoldings)
            override fun getTransactions() = flowOf(emptyList<PortfolioTransaction>())
            override suspend fun addTransaction(symbol: String, type: String, quantity: Double, price: Double) = Result.success(Unit)
            override suspend fun resetPortfolio() = Result.success(Unit)
        }
        val ideaRepo = object : IdeaCardRepository {
            override fun getAllIdeaCards(): Flow<List<IdeaCardEntity>> = flowOf(emptyList<IdeaCardEntity>())
            override suspend fun getIdeaCardForDate(dateString: String): IdeaCardEntity? = null
            override suspend fun generateDailyIdeaCards(): List<IdeaCardEntity> = listOf(
                IdeaCardEntity("1", "Idea 1", "Summary", "Rec", "STOCK", "LUCK", "2026-11-15", 0L)
            )
            override suspend fun initializeDefaultIdeaCards() {}
        }

        val viewModel = DashboardViewModel(
            GetStocksUseCase(psxRepo),
            GetPortfolioUseCase(portfolioRepo),
            GetFeedItemsUseCase(rssRepo),
            ideaRepo
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.stocks.size)
        assertEquals(1, state.feedItems.size)
        assertEquals(1, state.holdings.size)
        assertNotNull(state.recentIdea)
        assertEquals("LUCK", state.recentIdea?.targetSymbol)
        assertNull(state.errorMessage)
    }

    @Test
    fun testDashboardViewModelHandlesRepositoryExceptionGracefully() = runTest {
        val psxRepo = object : PsxRepository {
            override suspend fun getStocksState() = com.igent.mobile.domain.model.MarketDataState.Error("Network down")
            override suspend fun getStocks(): Result<List<Stock>> = Result.failure(Exception("Network down"))
            override suspend fun getStockDetail(symbol: String) = Result.failure<Stock>(Exception())
            override fun getStockTickerFlow() = flowOf(emptyList<Stock>())
        }
        val rssRepo = object : RssRepository {
            override suspend fun getFeedItems(): Result<List<FeedItem>> = Result.failure(Exception("RSS down"))
            override suspend fun refreshFeed() = Result.failure<Unit>(Exception())
        }
        val portfolioRepo = object : PortfolioRepository {
            override fun getHoldings() = flowOf(emptyList<PortfolioHolding>())
            override fun getTransactions() = flowOf(emptyList<PortfolioTransaction>())
            override suspend fun addTransaction(symbol: String, type: String, quantity: Double, price: Double) = Result.success(Unit)
            override suspend fun resetPortfolio() = Result.success(Unit)
        }
        val ideaRepo = object : IdeaCardRepository {
            override fun getAllIdeaCards(): Flow<List<IdeaCardEntity>> = flowOf(emptyList<IdeaCardEntity>())
            override suspend fun getIdeaCardForDate(dateString: String): IdeaCardEntity? = null
            override suspend fun generateDailyIdeaCards(): List<IdeaCardEntity> = emptyList<IdeaCardEntity>()
            override suspend fun initializeDefaultIdeaCards() {}
        }

        val viewModel = DashboardViewModel(
            GetStocksUseCase(psxRepo),
            GetPortfolioUseCase(portfolioRepo),
            GetFeedItemsUseCase(rssRepo),
            ideaRepo
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.stocks.isEmpty())
        assertTrue(state.feedItems.isEmpty())
    }
}
