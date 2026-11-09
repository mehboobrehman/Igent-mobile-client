package com.igent.mobile.data.repository

import com.igent.mobile.data.local.dao.WatchlistDao
import com.igent.mobile.data.local.entity.WatchlistEntity
import com.igent.mobile.domain.model.WatchlistItem
import com.igent.mobile.domain.repository.WatchlistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepositoryImpl @Inject constructor(
    private val watchlistDao: WatchlistDao
) : WatchlistRepository {

    override fun getWatchlist(): Flow<List<WatchlistItem>> {
        return watchlistDao.getWatchlist().map { list ->
            list.map {
                WatchlistItem(
                    symbol = it.symbol,
                    name = it.name,
                    addedAt = it.addedAt
                )
            }
        }
    }

    override suspend fun addToWatchlist(symbol: String, name: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val entity = WatchlistEntity(
                symbol = symbol.uppercase(),
                name = name,
                addedAt = System.currentTimeMillis()
            )
            watchlistDao.insertWatchlistItem(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFromWatchlist(symbol: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            watchlistDao.removeFromWatchlist(symbol.uppercase())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isWatchlisted(symbol: String): Boolean = withContext(Dispatchers.IO) {
        watchlistDao.isWatchlisted(symbol.uppercase()) > 0
    }
}
