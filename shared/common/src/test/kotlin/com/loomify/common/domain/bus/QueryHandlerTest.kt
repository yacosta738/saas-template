package com.loomify.common.domain.bus

import com.loomify.common.domain.bus.query.Query
import com.loomify.common.domain.bus.query.QueryHandler
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QueryHandlerTest {

    @Test
    fun async_queryHandler_should_retrieve_result() = runTest {
        class TestQuery(val id: Int) : Query<String>

        class TestQueryHandler : QueryHandler<TestQuery, String> {
            override suspend fun handle(query: TestQuery): String = "hello " + query.id
        }

        val handler = TestQueryHandler()
        val handlers: HashMap<Class<*>, Any> = hashMapOf(Pair(TestQueryHandler::class.java, handler))
        val provider = ManualDependencyProvider(handlers)
        val bus: Mediator = MediatorBuilder(provider).build()
        val result = bus.send(TestQuery(1))

        assertTrue {
            result == "hello 1"
        }
    }

    @Test
    fun should_throw_exception_if_given_async_query_has_not_been_registered_before() {
        class NonExistQuery : Query<String>

        val handlers: HashMap<Class<*>, Any> = hashMapOf()
        val provider = ManualDependencyProvider(handlers)
        val bus: Mediator = MediatorBuilder(provider).build()

        val exception = assertFailsWith(HandlerNotFoundException::class) {
            runTest {
                bus.send(NonExistQuery())
            }
        }

        assertNotNull(exception)
        assertEquals(
            "handler could not be found for ${NonExistQuery::class.java.typeName}",
            exception.message,
        )
    }

    @Nested
    inner class ParamaterizedTests {
        inner class ParameterizedQuery<TParam, TResponse>(val param: TParam) : Query<TResponse>

        inner class ParameterizedQueryHandler<TParam> :
            QueryHandler<ParameterizedQuery<TParam, String>, String> {
            override suspend fun handle(query: ParameterizedQuery<TParam, String>): String =
                query.param.toString()
        }

        @Test
        fun async_query_should_be_fired_and_return_result() = runTest {
            // given
            val handler = ParameterizedQueryHandler<ParameterizedQuery<Long, String>>()
            val handlers: HashMap<Class<*>, Any> =
                hashMapOf(Pair(ParameterizedQueryHandler::class.java, handler))
            val provider = ManualDependencyProvider(handlers)
            val bus: Mediator = MediatorBuilder(provider).build()

            // when
            val result = bus.send(ParameterizedQuery<Long, String>(61L))

            // then
            assertEquals("61", result)
        }
    }
}
