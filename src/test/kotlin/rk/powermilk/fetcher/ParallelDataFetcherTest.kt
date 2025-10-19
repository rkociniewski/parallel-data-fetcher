package rk.powermilk.fetcher

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import rk.powermilk.fetcher.model.DataSource
import rk.powermilk.fetcher.service.MockApiService
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParallelDataFetcherTest {

    @Test
    fun `should fetch from multiple sources in parallel`() = runTest {
        // Given
        val mockApi = mockk<MockApiService>()
        val fetcher = ParallelDataFetcher(mockApi)

        val sources = listOf(
            DataSource("Source1", "url1", 1),
            DataSource("Source2", "url2", 2),
            DataSource("Source3", "url3", 3)
        )

        coEvery { mockApi.fetch("url1") } coAnswers {
            delay(100)
            "Data from url1"
        }
        coEvery { mockApi.fetch("url2") } coAnswers {
            delay(50)
            "Data from url2"
        }
        coEvery { mockApi.fetch("url3") } coAnswers {
            delay(150)
            "Data from url3"
        }

        // When
        val results = fetcher.fetchFromMultipleSources(sources)

        // Then
        assertEquals(3, results.size)
        assertTrue(results.all { it.success })

        // Verify all fetches were called
        coVerify(exactly = 1) { mockApi.fetch("url1") }
        coVerify(exactly = 1) { mockApi.fetch("url2") }
        coVerify(exactly = 1) { mockApi.fetch("url3") }
    }

    @Test
    fun `should sort results by priority descending`() = runTest {
        // Given
        val mockApi = mockk<MockApiService>()
        val fetcher = ParallelDataFetcher(mockApi)

        val sources = listOf(
            DataSource("Low", "url1", 1),
            DataSource("Medium", "url2", 3),
            DataSource("High", "url3", 2)
        )

        coEvery { mockApi.fetch(any()) } returns "Data"

        // When
        val results = fetcher.fetchFromMultipleSources(sources)

        // Then
        assertEquals("Low", results[0].source)
        assertEquals("Medium", results[1].source)
        assertEquals("High", results[2].source)
    }

    @Test
    fun `should retry on IOException with exponential backoff`() = runTest {
        // Given
        val mockApi = mockk<MockApiService>()
        val fetcher = ParallelDataFetcher(mockApi)

        val sources = listOf(DataSource("Source1", "url1", 1))

        var callCount = 0
        coEvery { mockApi.fetch("url1") } coAnswers {
            callCount++
            if (callCount < 3) {
                throw IOException("Network error")
            }
            "Success on third try"
        }

        // When
        val results = fetcher.fetchFromMultipleSources(sources)

        // Then
        assertEquals(1, results.size)
        assertTrue(results[0].success)
        assertEquals("Success on third try", results[0].data)
        coVerify(exactly = 3) { mockApi.fetch("url1") }
    }

    @Test
    fun `should fail after max retries`() = runTest {
        // Given
        val mockApi = mockk<MockApiService>()
        val fetcher = ParallelDataFetcher(mockApi)

        val sources = listOf(DataSource("Source1", "url1", 1))

        coEvery { mockApi.fetch("url1") } throws IOException("Network error")

        // When
        val results = fetcher.fetchFromMultipleSources(sources)

        // Then
        assertEquals(1, results.size)
        assertFalse(results[0].success)
        assertEquals(null, results[0].data)
        coVerify(exactly = 3) { mockApi.fetch("url1") } // 3 attempts
    }

    @Test
    fun `should timeout if fetch takes too long`() = runTest {
        // Given
        val mockApi = mockk<MockApiService>()
        val fetcher = ParallelDataFetcher(mockApi)

        val sources = listOf(DataSource("Source1", "url1", 1))

        coEvery { mockApi.fetch("url1") } coAnswers {
            delay(10000) // Longer than timeout
            "Too late"
        }

        // When
        val results = fetcher.fetchFromMultipleSources(sources)

        // Then
        assertEquals(1, results.size)
        assertFalse(results[0].success)
        assertEquals(null, results[0].data)
    }

    @Test
    fun `should handle mixed success and failure`() = runTest {
        // Given
        val mockApi = mockk<MockApiService>()
        val fetcher = ParallelDataFetcher(mockApi)

        val sources = listOf(
            DataSource("Success", "url1", 1),
            DataSource("Failure", "url2", 2)
        )

        coEvery { mockApi.fetch("url1") } returns "Success data"
        coEvery { mockApi.fetch("url2") } throws IOException("Error")

        // When
        val results = fetcher.fetchFromMultipleSources(sources)

        // Then
        assertEquals(2, results.size)

        val successResult = results.find { it.source == "Success" }
        val failureResult = results.find { it.source == "Failure" }

        assertTrue(successResult!!.success)
        assertEquals("Success data", successResult.data)

        assertFalse(failureResult!!.success)
        assertEquals(null, failureResult.data)
    }

    @Test
    fun `should cancel all fetches when parent scope is cancelled`() = runTest {
        // Given
        val mockApi = mockk<MockApiService>()
        val fetcher = ParallelDataFetcher(mockApi)

        val sources = listOf(
            DataSource("Source1", "url1", 1),
            DataSource("Source2", "url2", 2)
        )

        coEvery { mockApi.fetch(any()) } coAnswers {
            delay(2000)
            "Data"
        }

        // When
        val job = launch {
            fetcher.fetchFromMultipleSources(sources)
        }

        delay(100) // Let it start
        job.cancel() // Cancel the operation

        // Then
        assertTrue(job.isCancelled)
    }

    @Test
    fun `should not retry on timeout cancellation`() = runTest {
        // Given
        val mockApi = mockk<MockApiService>()
        val fetcher = ParallelDataFetcher(mockApi)

        val sources = listOf(DataSource("Source1", "url1", 1))

        var callCount = 0
        coEvery { mockApi.fetch("url1") } coAnswers {
            callCount++
            delay(10000) // Always timeout
            "Never reached"
        }

        // When
        val results = fetcher.fetchFromMultipleSources(sources)

        // Then
        assertEquals(1, results.size)
        assertEquals(3, callCount)
        assertFalse(results[0].success)
        // Should attempt 3 times even with timeout
        coVerify(exactly = callCount) { mockApi.fetch("url1") }
    }

    @Test
    fun `should handle empty sources list`() = runTest {
        // Given
        val mockApi = mockk<MockApiService>()
        val fetcher = ParallelDataFetcher(mockApi)

        // When
        val results = fetcher.fetchFromMultipleSources(emptyList())

        // Then
        assertTrue(results.isEmpty())
    }

    @Test
    fun `should execute fetches concurrently not sequentially`() = runTest {
        // Given
        val mockApi = mockk<MockApiService>()
        val fetcher = ParallelDataFetcher(mockApi)

        val sources = listOf(
            DataSource("Source1", "url1", 1),
            DataSource("Source2", "url2", 2),
            DataSource("Source3", "url3", 3)
        )

        coEvery { mockApi.fetch(any()) } coAnswers {
            delay(1000)
            "Data"
        }

        // When
        val startTime = System.currentTimeMillis()
        val results = fetcher.fetchFromMultipleSources(sources)
        val duration = System.currentTimeMillis() - startTime

        // Then
        // If sequential, would take ~3000ms. If parallel, ~1000ms
        assertTrue(duration < 2000, "Expected parallel execution, took ${duration}ms")
        assertEquals(3, results.size)
    }
}
