package rk.powermilk.fetcher.model

/**
 * Represents a data source configuration for parallel fetching operations.
 *
 * This immutable data class encapsulates all information needed to identify
 * and prioritize a data source in a multi-source fetching scenario. Data sources
 * are typically API endpoints, microservices, or any remote resource that can
 * be accessed via a URL.
 *
 * ## Usage in Priority Systems
 * The [priority] field enables implementation of fallback strategies and
 * primary/secondary source patterns. Higher priority sources are preferred
 * when multiple sources succeed.
 *
 * ## Example: Primary-Backup Pattern
 * ```kotlin
 * val sources = listOf(
 *     DataSource("Primary API", "https://api.example.com/v1", priority = 10),
 *     DataSource("Backup API", "https://backup.example.com/v1", priority = 5),
 *     DataSource("Cache", "https://cache.example.com/v1", priority = 1)
 * )
 * ```
 *
 * ## Example: Geographic Distribution
 * ```kotlin
 * val sources = listOf(
 *     DataSource("US-East", "https://us-east.api.com", priority = 3),
 *     DataSource("EU-West", "https://eu-west.api.com", priority = 2),
 *     DataSource("AP-South", "https://ap-south.api.com", priority = 1)
 * )
 * ```
 *
 * @property name Human-readable identifier for the data source.
 *                Used for logging, debugging, and result identification.
 *                Should be unique within a fetch operation for clarity.
 * @property url The endpoint URL to fetch data from. Should be a valid HTTP/HTTPS URL.
 *               No validation is performed by this class; ensure URLs are valid
 *               before creating DataSource instances.
 * @property priority Numeric priority for result sorting (higher values = higher priority).
 *                    When multiple sources succeed, results are sorted by priority
 *                    in descending order. Can be any integer value, including negative.
 *                    Default: 0 (lowest priority)
 *
 * @constructor Creates a new DataSource with the specified name, URL, and priority.
 *
 * @author Rafał Kociniewski
 * @since 1.0.0
 *
 * @see FetchResult
 * @see rk.powermilk.fetcher.ParallelDataFetcher.fetchFromMultipleSources
 */
data class DataSource(
    val name: String,
    val url: String,
    val priority: Int,
)
