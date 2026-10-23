package com.igent.mobile.domain.usecase

import com.igent.mobile.domain.model.Message
import com.igent.mobile.domain.model.Stock
import com.igent.mobile.domain.model.FeedItem
import com.igent.mobile.domain.repository.AiRepository
import com.igent.mobile.domain.repository.PsxRepository
import com.igent.mobile.domain.repository.RssRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendAiMessageUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {
    suspend operator fun invoke(prompt: String): Result<Message> {
        if (prompt.isBlank()) {
            return Result.failure(IllegalArgumentException("Prompt cannot be blank"))
        }
        return aiRepository.sendMessage(prompt)
    }
}

class GetAiHistoryUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {
    operator fun invoke(): Flow<List<Message>> {
        return aiRepository.getMessageHistory()
    }
}

class GetStocksUseCase @Inject constructor(
    private val psxRepository: PsxRepository
) {
    suspend operator fun invoke(): Result<List<Stock>> {
        return psxRepository.getStocks()
    }
}

class GetStockDetailUseCase @Inject constructor(
    private val psxRepository: PsxRepository
) {
    suspend operator fun invoke(symbol: String): Result<Stock> {
        return psxRepository.getStockDetail(symbol)
    }
}

class GetFeedItemsUseCase @Inject constructor(
    private val rssRepository: RssRepository
) {
    suspend operator fun invoke(): Result<List<FeedItem>> {
        return rssRepository.getFeedItems()
    }
}
