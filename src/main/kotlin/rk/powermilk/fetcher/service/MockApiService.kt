package rk.powermilk.fetcher.service

import kotlinx.coroutines.delay
import rk.powermilk.fetcher.constant.Numbers
import java.io.IOException
import kotlin.random.Random

class MockApiService {
    suspend fun fetch(url: String): String {
        delay((Numbers.RANDOM_MIN..Numbers.RANDOM_MAX).random())
        if (Random.nextFloat() < Numbers.ERROR_RATE) {
            throw IOException("Network error")
        }
        return "Data from $url"
    }
}
