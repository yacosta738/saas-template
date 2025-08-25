package com.loomify

import com.loomify.engine.ReplaceCamelCase
import com.loomify.engine.authentication.infrastructure.TestSecurityConfiguration
import java.lang.annotation.Inherited
import org.junit.jupiter.api.DisplayNameGeneration
import org.junit.jupiter.api.Tag
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.annotation.AliasFor
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles

@WithMockUser
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@DisplayNameGeneration(ReplaceCamelCase::class)
@Inherited
@Tag("integration")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [LoomifyApplication::class, TestSecurityConfiguration::class],
)
@ActiveProfiles("test")
annotation class IntegrationTest(@get:AliasFor(annotation = SpringBootTest::class) val properties: Array<String> = [])
