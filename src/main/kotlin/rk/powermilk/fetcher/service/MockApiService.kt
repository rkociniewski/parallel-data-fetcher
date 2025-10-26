package rk.powermilk.fetcher.service

import kotlinx.coroutines.delay
import rk.powermilk.fetcher.constant.Numbers
import java.io.IOException
import kotlin.random.Random

/**
 * A mock API service that simulates network requests with random delays and failures.
 *
 * This class is designed for testing and demonstration purposes. It simulates realistic
 * network behavior including:
 * - Variable response times (10ms to 2000ms)
 * - Random failures (30% error rate)
 * - Network IO exceptions
 *
 * ## Testing Scenarios
 * This service enables testing of:
 * - **Timeout handling**: Responses sometimes exceed reasonable timeouts
 * - **Retry logic**: Random failures trigger retry mechanisms
 * - **Parallel execution**: Multiple concurrent requests
 * - **Error resilience**: Handling of IOException in production-like conditions
 *
 * ## Simulated Network Behavior
 * The service introduces:
 * 1. Random delay between [Numbers.RANDOM_MIN] and [Numbers.RANDOM_MAX] milliseconds
 * 2. Random failures with [Numbers.ERROR_RATE] probability (30%)
 * 3. Realistic error messages for failed requests
 *
 * ## Production Usage
 * **⚠️ Important**: This is a mock service and should NOT be used in production.
 * For production environments, implement a real API service that:
 * - Uses actual HTTP clients (e.g., Ktor, OkHttp)
 * - Implements proper authentication and authorization
 * - Handles real network protocols and error responses
 * - Provides proper logging and monitoring
 *
 * ## Example Usage
 * ```kotlin
 * val mockApi = MockApiService()
 *
 * // Simulate a fetch (may succeed or fail randomly)
 * try {
 *     val data = mockApi.fetch("https://api.example.com/data")
 *     println("Success: $data")
 * } catch (e: IOException) {
 *     println("Failed: ${e.message}")
 * }
 * ```
 *
 * ## Testing Configuration
 * Adjust behavior via [Numbers]:
 * ```kotlin
 * // In Numbers.kt
 * const val RANDOM_MIN: Long = 10      // Fast responses
 * const val RANDOM_MAX: Long = 2000    // Slow responses
 * const val ERROR_RATE: Float = 0.3F   // 30% failure rate
 * ```
 *
 * @author Rafał Kociniewski
 * @since 1.0.0
 *
 * @see Numbers.RANDOM_MIN
 * @see Numbers.RANDOM_MAX
 * @see Numbers.ERROR_RATE
 */
class MockApiService {

    /**
     * Simulates fetching data from a URL with random delay and potential failure.
     *
     * This suspend function mimics a real network request by:
     * 1. Introducing a random delay (simulating network latency)
     * 2. Randomly throwing IOException (simulating network failures)
     * 3. Returning simulated data on success
     *
     * ## Behavior
     * - **Delay**: Random duration between 10ms and 2000ms
     * - **Failure Rate**: 30% chance of throwing IOException
     * - **Success Response**: Returns "Data from {url}" string
     *
     * ## Error Simulation
     * When a failure occurs, throws [IOException] with message "Network error".
     * This simulates common network issues like:
     * - Connection timeouts
     * - Server unavailability
     * - Network interruptions
     * - DNS resolution failures
     *
     * ## Testing Patterns
     * ```kotlin
     * // Test successful fetch
     * val data = mockApi.fetch("https://api.example.com")
     * // May return: "Data from https://api.example.com"
     *
     * // Test with retry logic
     * repeat(3) { attempt ->
     *     try {
     *         val data = mockApi.fetch("https://api.example.com")
     *         println("Success on attempt ${attempt + 1}")
     *         return@repeat
     *     } catch (e: IOException) {
     *         println("Failed attempt ${attempt + 1}")
     *     }
     * }
     * ```
     *
     * ## Performance Characteristics
     * - **Minimum latency**: 10ms (best case)
     * - **Maximum latency**: 2000ms (worst case)
     * - **Average latency**: ~1005ms (mean of range)
     * - **Success rate**: ~70% (1 - ERROR_RATE)
     *
     * @param url The URL to fetch data from. Used only for generating response message;
     *            no actual network request is performed. Can be any string.
     * @return A string containing "Data from {url}" if the simulated fetch succeeds.
     *
     * @throws IOException If the simulated network request fails (30% probability).
     *                     Always thrown with message "Network error".
     *
     * @see Numbers.RANDOM_MIN
     * @see Numbers.RANDOM_MAX
     * @see Numbers.ERROR_RATE
     */
    suspend fun fetch(url: String): String {
        delay((Numbers.RANDOM_MIN..Numbers.RANDOM_MAX).random())
        if (Random.nextFloat() < Numbers.ERROR_RATE) {
            throw IOException("Network error")
        }
        return "Data from $url"
    }
}
