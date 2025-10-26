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

/**
 * A parallel data fetcher that retrieves data from multiple sources concurrently
 * with built-in retry logic, timeout handling, and priority-based sorting.
 *
 * This class orchestrates concurrent data fetching operations using Kotlin Coroutines,
 * providing resilience through automatic retries with exponential backoff for transient
 * failures and timeout protection to prevent hanging requests.
 *
 * ## Features
 * - **Parallel Execution**: Fetches from all sources simultaneously using `async`/`await`
 * - **Retry Mechanism**: Up to 3 attempts per source with exponential backoff for IO errors
 * - **Timeout Protection**: 5-second timeout per fetch attempt
 * - **Priority Sorting**: Results ordered by priority (descending)
 * - **Error Resilience**: Handles both timeout and IO exceptions gracefully
 *
 * ## Usage Example
 * ```kotlin
 * val api = MockApiService()
 * val fetcher = ParallelDataFetcher(api)
 *
 * val sources = listOf(
 *     DataSource("Primary", "https://api.example.com", priority = 3),
 *     DataSource("Backup", "https://backup.example.com", priority = 2)
 * )
 *
 * val results = fetcher.fetchFromMultipleSources(sources)
 * results.forEach { result ->
 *     if (result.success) {
 *         println("${result.source}: ${result.data}")
 *     }
 * }
 * ```
 *
 * ## Performance Characteristics
 * - Time complexity: O(max(t₁, t₂, ..., tₙ)) for parallel execution
 * - Space complexity: O(n) where n is the number of sources
 * - Maximum duration per source: 15 seconds (3 retries × 5 second timeout)
 *
 * @property api The API service used to fetch data from remote sources
 *
 * @constructor Creates a new ParallelDataFetcher with the specified API service
 *
 * @author Rafał Kociniewski
 * @since 1.0.0
 */
class ParallelDataFetcher(private val api: MockApiService) {

    /**
     * Fetches data from multiple sources in parallel and returns sorted results.
     *
     * This suspend function creates a coroutine scope and launches concurrent fetch
     * operations for each provided data source. Each fetch operation includes automatic
     * retry logic and timeout handling. Results are automatically sorted by priority
     * in descending order (highest priority first).
     *
     * ## Execution Flow
     * 1. Creates async coroutines for each source
     * 2. Each coroutine executes [fetchWithRetry] with retry logic
     * 3. Awaits all coroutines to complete
     * 4. Sorts results by priority (descending)
     *
     * ## Error Handling
     * - Individual fetch failures don't cancel other operations
     * - Failed fetches return [FetchResult] with `success = false`
     * - All exceptions are caught and converted to failed results
     *
     * ## Example
     * ```kotlin
     * val sources = listOf(
     *     DataSource("API-1", "https://api1.example.com", 1),
     *     DataSource("API-2", "https://api2.example.com", 2),
     *     DataSource("API-3", "https://api3.example.com", 3)
     * )
     *
     * val results = fetcher.fetchFromMultipleSources(sources)
     *
     * // Results are sorted by priority: [API-3, API-2, API-1]
     * val successfulResults = results.filter { it.success }
     * val primaryResult = results.first() // Highest priority
     * ```
     *
     * @param sources List of data sources to fetch from. Can be empty.
     * @return List of [FetchResult] objects sorted by priority (descending).
     *         Returns empty list if sources is empty.
     *
     * @throws CancellationException if the parent coroutine scope is cancelled
     *
     * @see DataSource
     * @see FetchResult
     * @see fetchWithRetry
     */
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

    /**
     * Fetches data from a single source with automatic retry logic and exponential backoff.
     *
     * This private suspend function implements a retry mechanism with different strategies
     * for different types of failures:
     * - **Timeout errors**: Retry immediately without delay
     * - **IO errors**: Retry with exponential backoff (100ms, 200ms, 400ms)
     * - **Success**: Return immediately without further attempts
     *
     * ## Retry Strategy
     * - Maximum retry attempts: 3 (configurable via parameter)
     * - Timeout per attempt: 5 seconds ([Numbers.WITH_TIMEOUT])
     * - Backoff formula: `BACKOFF * 2^attempt` for IO errors
     * - No backoff for timeout errors (immediate retry)
     *
     * ## Timeout Handling
     * Each fetch attempt is wrapped in a timeout using [withTimeout]. If the fetch
     * exceeds the timeout, a [TimeoutCancellationException] is caught and the operation
     * is retried without delay.
     *
     * ## Error Flow
     * ```
     * Attempt 1: Fail (IOException) -> Wait 100ms
     * Attempt 2: Fail (IOException) -> Wait 200ms
     * Attempt 3: Fail (IOException) -> Return failure
     * ```
     *
     * @param source The data source to fetch from
     * @param maxRetries Maximum number of retry attempts (default: 3)
     * @return [FetchResult] with success status and data (if successful) or null (if failed)
     *
     * @see DataSource
     * @see FetchResult
     * @see Numbers.WITH_TIMEOUT
     * @see Numbers.BACKOFF
     */
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
