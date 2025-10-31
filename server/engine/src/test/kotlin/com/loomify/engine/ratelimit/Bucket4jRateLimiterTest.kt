package com.loomify.engine.ratelimit

import com.loomify.engine.ratelimit.domain.RateLimitResult
import com.loomify.engine.ratelimit.infrastructure.Bucket4jRateLimiter
import com.loomify.engine.ratelimit.infrastructure.RateLimitStrategy
import com.loomify.engine.ratelimit.infrastructure.config.BucketConfigurationStrategy
import com.loomify.engine.ratelimit.infrastructure.config.RateLimitProperties
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.Duration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier

/**
 * Unit tests for Bucket4jRateLimiter.
 *
 * Tests cover:
 * - Token consumption for AUTH and BUSINESS strategies
 * - Rate limit denial and allowed scenarios
 * - Bucket caching per identifier and strategy
 * - Correct mapping to RateLimitResult
 * - Token refill behavior
 */
class Bucket4jRateLimiterTest {

    private lateinit var rateLimiter: Bucket4jRateLimiter
    private lateinit var properties: RateLimitProperties

    @BeforeEach
    fun setUp() {
        properties = RateLimitProperties(
            enabled = true,
            auth = RateLimitProperties.AuthRateLimitConfig(
                enabled = true,
                limits = listOf(
                    RateLimitProperties.BandwidthLimit(
                        name = "per-minute",
                        capacity = 5, // Small capacity for testing
                        refillTokens = 5,
                        refillDuration = Duration.ofMinutes(1),
                    ),
                ),
            ),
            business = RateLimitProperties.BusinessRateLimitConfig(
                enabled = true,
                pricingPlans = mapOf(
                    "free" to RateLimitProperties.BandwidthLimit(
                        name = "free-plan",
                        capacity = 3, // Small capacity for testing
                        refillTokens = 3,
                        refillDuration = Duration.ofHours(1),
                    ),
                    "basic" to RateLimitProperties.BandwidthLimit(
                        name = "basic-plan",
                        capacity = 5,
                        refillTokens = 5,
                        refillDuration = Duration.ofHours(1),
                    ),
                    "professional" to RateLimitProperties.BandwidthLimit(
                        name = "professional-plan",
                        capacity = 10,
                        refillTokens = 10,
                        refillDuration = Duration.ofHours(1),
                    ),
                ),
            ),
        )
        val configStrategy = BucketConfigurationStrategy(properties)
        rateLimiter = Bucket4jRateLimiter(configStrategy)
    }

    @Test
    fun `should allow token consumption when under limit for AUTH strategy`() {
        // Given
        val identifier = "IP:192.168.1.1"

        // When/Then
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 4 // 5 capacity - 1 consumed
            }
            .verifyComplete()
    }

    @Test
    fun `should deny token consumption when limit exceeded for AUTH strategy`() {
        // Given
        val identifier = "IP:192.168.1.2"

        // Consume all tokens
        repeat(5) {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH).block()
        }

        // When/Then - next request should be denied
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Denied>()
                result.retryAfter.seconds shouldBeGreaterThanOrEqual 0
            }
            .verifyComplete()
    }

    @Test
    fun `should allow token consumption when under limit for BUSINESS strategy with FREE plan`() {
        // Given
        val identifier = "FREE-KEY-123"

        // When/Then
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 2 // 3 capacity - 1 consumed
            }
            .verifyComplete()
    }

    @Test
    fun `should deny token consumption when limit exceeded for BUSINESS strategy`() {
        // Given
        val identifier = "FREE-KEY-456"

        // Consume all tokens
        repeat(3) {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS).block()
        }

        // When/Then - next request should be denied
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Denied>()
                result.retryAfter.seconds shouldBeGreaterThanOrEqual 0
            }
            .verifyComplete()
    }

    @Test
    fun `should allow token consumption for BASIC plan`() {
        // Given
        val identifier = "BX001-BASIC-KEY"

        // When/Then
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 4 // 5 capacity - 1 consumed
            }
            .verifyComplete()
    }

    @Test
    fun `should allow token consumption for PROFESSIONAL plan`() {
        // Given
        val identifier = "PX001-PRO-KEY"

        // When/Then
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 9 // 10 capacity - 1 consumed
            }
            .verifyComplete()
    }

    @Test
    fun `should maintain separate buckets for different identifiers with same strategy`() {
        // Given
        val identifier1 = "IP:192.168.1.1"
        val identifier2 = "IP:192.168.1.2"

        // When - consume tokens for identifier1
        repeat(5) {
            rateLimiter.consumeToken(identifier1, RateLimitStrategy.AUTH).block()
        }

        // Then - identifier2 should still have tokens available
        StepVerifier.create(rateLimiter.consumeToken(identifier2, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
            }
            .verifyComplete()
    }

    @Test
    fun `should maintain separate buckets for same identifier with different strategies`() {
        // Given
        val identifier = "TEST-KEY"

        // When - consume all AUTH tokens
        repeat(5) {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH).block()
        }

        // Then - BUSINESS strategy should still have tokens available
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.BUSINESS))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
            }
            .verifyComplete()
    }

    @Test
    fun `should decrement remaining tokens with each consumption`() {
        // Given
        val identifier = "IP:192.168.1.3"

        // When/Then - first consumption
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 4
            }
            .verifyComplete()

        // When/Then - second consumption
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 3
            }
            .verifyComplete()

        // When/Then - third consumption
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 2
            }
            .verifyComplete()
    }

    @Test
    fun `should use default BUSINESS strategy when calling consumeToken with identifier only`() {
        // Given
        val identifier = "DEFAULT-KEY"

        // When/Then
        StepVerifier.create(rateLimiter.consumeToken(identifier))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                // Should use FREE plan (3 tokens) as default
                result.remainingTokens shouldBe 2
            }
            .verifyComplete()
    }

    @Test
    fun `should cache buckets for repeated requests`() {
        // Given
        val identifier = "CACHED-KEY"

        // When - make multiple requests
        val result1 = rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH).block()
        val result2 = rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH).block()

        // Then - should see token decrements indicating same bucket is being used
        result1.shouldBeInstanceOf<RateLimitResult.Allowed>()
        result1.remainingTokens shouldBe 4

        result2.shouldBeInstanceOf<RateLimitResult.Allowed>()
        result2.remainingTokens shouldBe 3
    }

    @Test
    fun `should handle IP-based identifiers correctly`() {
        // Given
        val ipIdentifier = "IP:10.0.0.1"

        // When/Then
        StepVerifier.create(rateLimiter.consumeToken(ipIdentifier, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
            }
            .verifyComplete()
    }

    @Test
    fun `should handle API key identifiers correctly`() {
        // Given
        val apiKeyIdentifier = "PX001-ABC123"

        // When/Then
        StepVerifier.create(rateLimiter.consumeToken(apiKeyIdentifier, RateLimitStrategy.BUSINESS))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Allowed>()
                result.remainingTokens shouldBe 9 // Professional plan (10 tokens)
            }
            .verifyComplete()
    }

    @Test
    fun `should return non-negative retry after duration when denied`() {
        // Given
        val identifier = "RETRY-TEST"

        // Consume all tokens
        repeat(5) {
            rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH).block()
        }

        // When/Then
        StepVerifier.create(rateLimiter.consumeToken(identifier, RateLimitStrategy.AUTH))
            .assertNext { result ->
                result.shouldBeInstanceOf<RateLimitResult.Denied>()
                result.retryAfter.isNegative shouldBe false
                result.retryAfter.isZero shouldBe false
            }
            .verifyComplete()
    }
}
