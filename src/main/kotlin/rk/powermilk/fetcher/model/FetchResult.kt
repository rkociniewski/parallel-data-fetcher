package rk.powermilk.fetcher.model

/**
 * Represents the result of a data fetch operation from a single source.
 *
 * This immutable data class encapsulates the outcome of fetching data from a
 * remote source, including success/failure status, retrieved data (if any),
 * and priority information for result sorting.
 *
 * ## Success Scenarios
 * When a fetch succeeds, [success] is `true` and [data] contains the retrieved
 * content. The [source] identifies which data source provided the result.
 *
 * ## Failure Scenarios
 * When a fetch fails (after all retry attempts), [success] is `false` and
 * [data] is `null`. Failures can occur due to:
 * - Network timeouts (exceeding [Numbers.WITH_TIMEOUT])
 * - IO exceptions (network errors, connection refused, etc.)
 * - Maximum retry attempts exceeded
 *
 * ## Priority-Based Sorting
 * Results are automatically sorted by [priority] in descending order by
 * [ParallelDataFetcher.fetchFromMultipleSources], allowing consumers to
 * easily identify the most preferred successful result.
 *
 * ## Example: Processing Results
 * ```kotlin
 * val results: List<FetchResult> = fetcher.fetchFromMultipleSources(sources)
 *
 * // Get primary result (highest priority success)
 * val primary = results.firstOrNull { it.success }
 *
 * // Handle all results
 * results.forEach { result ->
 *     when {
 *         result.success -> println("✓ ${result.source}: ${result.data}")
 *         else -> println("✗ ${result.source}: Failed")
 *     }
 * }
 *
 * // Fallback strategy
 * val data = results
 *     .firstOrNull { it.success }?.data
 *     ?: "Default fallback data"
 * ```
 *
 * ## Example: Success Rate Analysis
 * ```kotlin
 * val successRate = results.count { it.success }.toFloat() / results.size
 * println("Success rate: ${successRate * 100}%")
 *
 * val failures = results.filter { !it.success }
 * println("Failed sources: ${failures.map { it.source }}")
 * ```
 *
 * @property source Name of the data source that produced this result.
 *                  Matches the [DataSource.name] from the original request.
 *                  Used for logging, debugging, and identifying result origin.
 * @property data The fetched data as a String, or `null` if the fetch failed.
 *                When [success] is `true`, this should contain the response data.
 *                When [success] is `false`, this is always `null`.
 * @property success Indicates whether the fetch operation succeeded.
 *                   `true` if data was successfully retrieved,
 *                   `false` if the operation failed after all retry attempts.
 * @property priority Numeric priority inherited from the source's [DataSource.priority].
 *                    Used for sorting results in descending order (highest first).
 *                    Default: 0 (lowest priority)
 *
 * @constructor Creates a new FetchResult with the specified source, data, success status, and priority.
 *
 * @author Rafał Kociniewski
 * @since 1.0.0
 *
 * @see DataSource
 * @see rk.powermilk.fetcher.ParallelDataFetcher.fetchFromMultipleSources
 * @see rk.powermilk.fetcher.constant.Numbers
 */
data class FetchResult(
    val source: String,
    val data: String?,
    val success: Boolean,
    val priority: Int = 0,
)
