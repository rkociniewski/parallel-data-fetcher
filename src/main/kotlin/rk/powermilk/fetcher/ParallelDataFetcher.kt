package rk.powermilk.fetcher

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import rk.powermilk.fetcher.constant.Numbers
import rk.powermilk.fetcher.model.DataSource
import rk.powermilk.fetcher.model.FetchResult
import rk.powermilk.fetcher.service.MockApiService
import java.io.IOException

class ParallelDataFetcher(private val api: MockApiService) {

    suspend fun fetchFromMultipleSources(
        sources: List<DataSource>
    ): List<FetchResult> = coroutineScope {
        sources
            .map {
                async {
                    fetchWithRetry(it)
                }
            }
            .awaitAll()
            .sortedByDescending { it.priority }
    }

    private suspend fun fetchWithRetry(
        source: DataSource,
        maxRetries: Int = 3
    ): FetchResult {
        var lastException: Exception? = null

        repeat(maxRetries) {
            val attemptResult = runCatching {
                withTimeout(Numbers.WITH_TIMEOUT) {
                    api.fetch(source.url)
                }
            }

            when {
                attemptResult.isSuccess -> {
                    return FetchResult(source.name, attemptResult.getOrNull(), true)
                }

                attemptResult.exceptionOrNull() is TimeoutCancellationException -> {
                    lastException = attemptResult.exceptionOrNull() as TimeoutCancellationException
                    println("Timeout on attempt $it for ${source.name}: ${lastException.message}")
                }

                attemptResult.exceptionOrNull() is IOException -> {
                    lastException = attemptResult.exceptionOrNull() as IOException
                    println("IO error on attempt $it for ${source.name}: ${lastException.message}")
                    if (it < maxRetries - 1) {
                        delay(Numbers.BACKOFF * (1 shl it))
                    }
                }
            }
        }

        return FetchResult(source.name, null, false).also {
            lastException?.let {
                println("Failed after $maxRetries attempts for ${source.name}: ${it.message}")
            }
        }
    }
}
