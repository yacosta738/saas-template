package com.loomify.engine.users.domain.event

import com.loomify.common.domain.bus.event.BaseDomainEvent

/**
 * Represents an event that is triggered when a user is created.
 *
 * This event contains the details of the newly created user, including their ID, email,
 * first name, and last name.
 *
 * @created 8/7/23
 * @property id The unique identifier of the user.
 * @property email The email address of the user.
 * @property firstName The first name of the user, nullable if not provided.
 * @property lastName The last name of the user, nullable if not provided.
 */
data class UserCreatedEvent(
    val id: String,
    val email: String,
    val firstName: String?,
    val lastName: String?
) : BaseDomainEvent()
