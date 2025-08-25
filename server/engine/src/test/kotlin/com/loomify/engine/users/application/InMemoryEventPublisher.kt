package com.loomify.engine.users.application

import com.loomify.common.domain.bus.event.DomainEvent
import com.loomify.common.domain.bus.event.EventPublisher
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

/**
 * In memory event publisher for testing purposes
 *
 * @created 3/7/23
 */
class InMemoryEventPublisher<T : DomainEvent>(
    private val events: BlockingQueue<T> = LinkedBlockingQueue()
) : EventPublisher<T> {
    override suspend fun publish(event: T) {
        events.add(event)
    }

    fun getEvents(): List<T> = events.toList()
}
