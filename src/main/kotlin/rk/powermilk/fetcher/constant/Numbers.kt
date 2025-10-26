package rk.powermilk.fetcher.constant

/**
 * Configuration constants for the Parallel Data Fetcher library.
 *
 * This object contains all timing, threshold, and probability constants used throughout
 * the application for timeout management, retry backoff calculations, and mock API behavior.
 *
 * ## Usage Categories
 * - **Timeout Management**: [WITH_TIMEOUT]
 * - **Retry Strategy**: [BACKOFF]
 * - **Mock API Simulation**: [RANDOM_MIN], [RANDOM_MAX], [ERROR_RATE]
 *
 * ## Modification Guidelines
 * When modifying these values, consider:
 * - Network latency in your deployment environment
 * - Expected response times from data sources
 * - Trade-off between responsiveness and reliability
 * - Testing scenarios and mock behavior requirements
 *
 * @author Rafał Kociniewski
 * @since 1.0.0
 */
object Numbers {

    /**
     * Maximum timeout duration for a single fetch attempt in milliseconds.
     *
     * If a fetch operation exceeds this duration, it will be cancelled with a
     * [kotlinx.coroutines.TimeoutCancellationException] and retried. This prevents indefinite hanging
     * and ensures predictable maximum latency.
     *
     * ## Impact on System Behavior
     * - **Higher values**: More tolerance for slow networks, but potentially longer wait times
     * - **Lower values**: Faster failure detection, but may timeout valid slow requests
     *
     * ## Calculation Example
     * With 3 retry attempts, maximum duration per source = 3 × 5000ms = 15 seconds
     *
     * @see rk.powermilk.fetcher.ParallelDataFetcher.fetchWithRetry
     */
    const val WITH_TIMEOUT = 5000L

    /**
     * Base delay in milliseconds for exponential backoff between retry attempts.
     *
     * This value is used as the base for calculating exponential backoff delays
     * when retrying failed fetch operations due to IO errors.
     *
     * ## Backoff Formula
     * ```
     * delay = BACKOFF * 2^attemptNumber
     * ```
     *
     * ## Example Progression
     * - Attempt 1 fails: Wait 100ms (100 × 2⁰)
     * - Attempt 2 fails: Wait 200ms (100 × 2¹)
     * - Attempt 3 fails: Wait 400ms (100 × 2²)
     *
     * ## Tuning Guidelines
     * - **Higher values**: Longer waits between retries, may help overloaded servers recover
     * - **Lower values**: Faster retry cycles, but may overwhelm already struggling services
     *
     * @see rk.powermilk.fetcher.ParallelDataFetcher.fetchWithRetry
     */
    const val BACKOFF = 100L

    /**
     * Minimum random delay in milliseconds for mock API responses.
     *
     * This constant defines the lower bound for simulated network latency
     * in the [MockApiService]. Used only for testing and demonstration purposes.
     *
     * ## Testing Use Case
     * Simulates fast network responses to test best-case scenario performance.
     *
     * @see rk.powermilk.fetcher.service.MockApiService.fetch
     * @see RANDOM_MAX
     */
    const val RANDOM_MIN = 10L

    /**
     * Maximum random delay in milliseconds for mock API responses.
     *
     * This constant defines the upper bound for simulated network latency
     * in the [MockApiService]. Used only for testing and demonstration purposes.
     *
     * ## Testing Use Case
     * Simulates slow network responses to test:
     * - Timeout handling
     * - Parallel execution efficiency
     * - User experience with slow sources
     *
     * ## Example
     * With range [RANDOM_MIN]...[RANDOM_MAX], response times vary between 10ms and 2000ms,
     * simulating a realistic mix of fast and slow network conditions.
     *
     * @see rk.powermilk.fetcher.service.MockApiService.fetch
     * @see RANDOM_MIN
     */
    const val RANDOM_MAX = 2000L

    /**
     * Probability of simulated errors in the mock API (0.0 to 1.0).
     *
     * This constant determines the likelihood that a mock API call will throw
     * an [IOException] instead of returning data. Used exclusively for testing
     * error handling and retry logic.
     *
     * ## Current Setting
     * `0.3F` means approximately 30% of mock API calls will fail with an IOException.
     *
     * ## Testing Scenarios
     * - **0.0**: No errors (test happy path)
     * - **0.3**: Moderate failure rate (test retry logic)
     * - **1.0**: Always fail (test exhaustive retry and failure handling)
     *
     * ## Example Usage
     * ```kotlin
     * if (Random.nextFloat() < ERROR_RATE) {
     *     throw IOException("Network error")
     * }
     * ```
     *
     * @see rk.powermilk.fetcher.service.MockApiService.fetch
     */
    const val ERROR_RATE = 0.3f
}
