# 🚀 Parallel Data Fetcher

[![version](https://img.shields.io/badge/version-1.0.15-yellow.svg)](https://semver.org)
[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)
[![Build](https://github.com/rkociniewski/parallel-data-fetcher/actions/workflows/main.yml/badge.svg)](https://github.com/rkociniewski/rosario/actions/workflows/main.yml)
[![CodeQL](https://github.com/rkociniewski/parallel-data-fetcher/actions/workflows/codeql.yml/badge.svg)](https://github.com/rkociniewski/rosario/actions/workflows/codeql.yml)
[![Dependabot Status](https://img.shields.io/badge/Dependabot-enabled-success?logo=dependabot)](https://github.com/rkociniewski/parallel-data-fetcher/network/updates)
[![codecov](https://codecov.io/gh/rkociniewski/parallel-data-fetcher/branch/main/graph/badge.svg)](https://codecov.io/gh/rkociniewski/rosario)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blueviolet?logo=kotlin)](https://kotlinlang.org/)
[![Gradle](https://img.shields.io/badge/Gradle-9.1.0-blue?logo=gradle)](https://gradle.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-greem.svg)](https://opensource.org/licenses/MIT)

**Parallel Data Fetcher** is a robust Kotlin library for concurrent data fetching from multiple sources with advanced retry mechanisms, timeout handling, and priority-based result sorting. Built with Kotlin Coroutines for efficient parallel processing.

## ✨ Features

* 🔄 **Parallel Fetching** - Fetch data from multiple sources concurrently using Kotlin Coroutines
* ⚡ **Retry Logic** - Intelligent retry mechanism with exponential backoff for transient failures
* ⏱️ **Timeout Management** - Configurable timeouts to prevent hanging requests
* 📊 **Priority Sorting** - Automatic result sorting based on source priority
* 🛡️ **Error Handling** - Comprehensive error handling for IO exceptions and timeouts
* 🧪 **Fully Tested** - Extensive test coverage with MockK and Turbine
* 📚 **KDoc Documentation** - Complete API documentation with examples

## 🎯 Use Cases

- Fetching data from multiple microservices simultaneously
- Load balancing across multiple API endpoints
- Implementing fallback strategies for high-availability systems
- Aggregating data from distributed sources
- Testing parallel processing scenarios

## 📋 Core Components

### ParallelDataFetcher

The main class that orchestrates parallel data fetching with retry logic:

```kotlin
val api = MockApiService()
val fetcher = ParallelDataFetcher(api)

val sources = listOf(
    DataSource("Primary", "https://api.example.com/v1", priority = 3),
    DataSource("Secondary", "https://backup.example.com/v1", priority = 2),
    DataSource("Cache", "https://cache.example.com/v1", priority = 1)
)

val results = fetcher.fetchFromMultipleSources(sources)
```

### Key Features

- **Concurrent Execution**: Uses `async` and `awaitAll` for true parallel processing
- **Retry Strategy**: Up to 3 attempts per source with exponential backoff
- **Timeout Protection**: 5-second timeout per fetch attempt
- **Priority Ordering**: Results sorted by priority (highest first)

## 🚀 Getting Started

### Requirements

* JDK 21 or later
* Kotlin 2.2.21+
* Gradle 9.1.0+

### Installation

1. Clone the repository:

   ```bash
   git clone git@github.com:yourusername/parallel-data-fetcher.git
   cd parallel-data-fetcher
   ```

2. Build the project:

   ```bash
   ./gradlew build
   ```

3. Run tests:

   ```bash
   ./gradlew test
   ```

## 💻 Usage Examples

### Basic Fetching

```kotlin
import rk.powermilk.fetcher.ParallelDataFetcher
import rk.powermilk.fetcher.model.DataSource
import rk.powermilk.fetcher.service.MockApiService

suspend fun main() {
    val api = MockApiService()
    val fetcher = ParallelDataFetcher(api)

    val sources = listOf(
        DataSource("API-1", "https://api1.example.com", 1),
        DataSource("API-2", "https://api2.example.com", 2)
    )

    val results = fetcher.fetchFromMultipleSources(sources)

    results.forEach { result ->
        if (result.success) {
            println("${result.source}: ${result.data}")
        } else {
            println("${result.source}: Failed")
        }
    }
}
```

### Handling Results

```kotlin
val results = fetcher.fetchFromMultipleSources(sources)

// Filter successful results
val successful = results.filter { it.success }

// Get highest priority result
val primary = results.firstOrNull { it.success }

// Handle failures
val failures = results.filter { !it.success }
```

## 🔧 Architecture

### Design Patterns

* **Coroutines** - Structured concurrency with `coroutineScope`
* **Async/Await** - Parallel execution with `async` and `awaitAll`
* **Retry Pattern** - Exponential backoff with configurable retries
* **Circuit Breaker** - Timeout protection prevents cascading failures

### Error Handling Strategy

1. **Timeout Errors** - Retry without delay
2. **IO Errors** - Retry with exponential backoff (100ms, 200ms, 400ms)
3. **Other Errors** - Propagate as failure result

## 🗂 Project Structure

```
📦rk.powermilk.fetcher
 ┣ 📁constant        # Configuration constants
 ┃ ┗ 📜Numbers.kt    # Timeout, backoff, and threshold values
 ┣ 📁model           # Data models
 ┃ ┣ 📜DataSource.kt # Source configuration (name, URL, priority)
 ┃ ┗ 📜FetchResult.kt # Result wrapper (source, data, success)
 ┣ 📁service         # API services
 ┃ ┗ 📜MockApiService.kt # Mock implementation for testing
 ┗ 📜ParallelDataFetcher.kt # Main fetcher class
```

## ⚙️ Configuration

### Constants (Numbers.kt)

| Constant       | Value     | Description                        |
|----------------|-----------|------------------------------------|
| `WITH_TIMEOUT` | 5000ms    | Maximum time per fetch attempt     |
| `BACKOFF`      | 100ms     | Base delay for exponential backoff |
| `RANDOM_MIN`   | 10ms      | Minimum simulated delay (MockApi)  |
| `RANDOM_MAX`   | 2000ms    | Maximum simulated delay (MockApi)  |
| `ERROR_RATE`   | 0.3 (30%) | Simulated error rate (MockApi)     |

### Customization

To customize retry behavior, modify the `fetchWithRetry` function parameters:

```kotlin
private suspend fun fetchWithRetry(
    source: DataSource,
    maxRetries: Int = 3  // Change this value
): FetchResult
```

## 🛠️ Development

### Running Tests

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew coverage

# Generate coverage report
./gradlew jacocoTestReport
```

### Code Quality

```bash
# Run Detekt static analysis
./gradlew detekt

# Generate KDoc documentation
./gradlew dokkaHtml
```

### Test Coverage

The project maintains **75%+ code coverage** with comprehensive test scenarios:

- ✅ Parallel execution
- ✅ Priority sorting
- ✅ Retry logic with exponential backoff
- ✅ Timeout handling
- ✅ Mixed success/failure scenarios
- ✅ Edge cases (empty lists, cancellation)

## 📦 Dependencies

```kotlin
dependencies {
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("io.mockk:mockk:1.13.14")
    testImplementation("app.cash.turbine:turbine:1.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}
```

## 🧪 Testing

### Test Examples

#### Parallel Execution Test

```kotlin
@Test
fun `should fetch from multiple sources in parallel`() = runTest {
    val results = fetcher.fetchFromMultipleSources(sources)
    assertEquals(3, results.size)
    assertTrue(results.all { it.success })
}
```

#### Retry Logic Test

```kotlin
@Test
fun `should retry on IOException with exponential backoff`() = runTest {
    // Simulates failure on first 2 attempts, success on 3rd
    val results = fetcher.fetchFromMultipleSources(sources)
    assertTrue(results[0].success)
    coVerify(exactly = 3) { mockApi.fetch("url1") }
}
```

## 📊 Performance Characteristics

- **Concurrent Execution**: O(max(t₁, t₂, ..., tₙ)) instead of O(t₁ + t₂ + ... + tₙ)
- **Memory**: O(n) where n is number of sources
- **Timeouts**: Maximum 15 seconds per source (3 retries × 5 seconds)
- **Backoff**: 100ms, 200ms, 400ms for successive retries

## 🔐 Security

- ⚠️ **No built-in authentication** - Implement in your API service
- ⚠️ **No input validation** - URLs should be validated before use
- ⚠️ **No rate limiting** - Consider adding for production use

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Add tests for new functionality
4. Ensure all tests pass (`./gradlew test`)
5. Maintain code coverage above 75%
6. Submit a Pull Request

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable names
- Add KDoc comments for public APIs
- Write unit tests for new features

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🏗️ Built With

* [Kotlin](https://kotlinlang.org/) - Programming language
* [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - Asynchronous programming
* [Gradle](https://gradle.org/) - Build system
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [MockK](https://mockk.io/) - Mocking library
* [Dokka](https://github.com/Kotlin/dokka) - Documentation engine
* [Detekt](https://detekt.dev/) - Static code analysis

## 📋 Versioning

We use [Semantic Versioning](http://semver.org/) for versioning.

Version format: `MAJOR.MINOR.PATCH`

- **MAJOR**: Breaking API changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes

## 👨‍💻 Authors

* **Rafał Kociniewski** - [rkociniewski](https://github.com/rkociniewski)

## 🙏 Acknowledgments

* Inspired by modern distributed systems patterns
* Built with Kotlin Coroutines best practices
* Test patterns from coroutines testing guide

## 📚 Documentation

* [KDoc API Documentation](docs/kdoc/) - Generated API documentation
* [Architecture Guide](docs/ARCHITECTURE.md) - Detailed architecture overview
* [Testing Guide](docs/TESTING.md) - Testing strategies and patterns

## 📞 Support

* **Issues**: [GitHub Issues](https://github.com/yourusername/parallel-data-fetcher/issues)
* **Documentation**: [KDoc](docs/kdoc/)

## 🚦 Project Status

**Status**: ✅ Active Development
**Latest Version**: 1.0.13
**Test Coverage**: 75%+
**Code Quality**: Detekt passing

---

Made with ❤️ and 🙏 by [Rafał Kociniewski](https://github.com/rkociniewski)
