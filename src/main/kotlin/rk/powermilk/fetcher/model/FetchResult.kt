package rk.powermilk.fetcher.model

data class FetchResult(
    val source: String,
    val data: String?,
    val success: Boolean,
    val priority: Int = 0,
)
