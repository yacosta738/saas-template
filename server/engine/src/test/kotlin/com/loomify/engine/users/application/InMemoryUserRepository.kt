package com.loomify.engine.users.application

import com.loomify.common.domain.vo.credential.Credential
import com.loomify.common.domain.vo.email.Email
import com.loomify.common.domain.vo.name.FirstName
import com.loomify.common.domain.vo.name.LastName
import com.loomify.engine.users.domain.User
import com.loomify.engine.users.domain.UserCreator
import com.loomify.engine.users.domain.UserStoreException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InMemoryUserRepository(
    private val users: MutableMap<String, User> = ConcurrentHashMap()
) : UserCreator {

    /**
     * Create a new user.
     *
     * @param email The user's email.
     * @param credential The user's credential.
     * @param firstName Optional first name.
     * @param lastName Optional last name.
     * @return The created User object.
     * @throws UserStoreException if a user with the same email or username already exists.
     */
    override suspend fun create(
        email: Email,
        credential: Credential,
        firstName: FirstName?,
        lastName: LastName?
    ): User {
        if (checkIfUserExist(email)) {
            throw UserStoreException(
                "User with email: ${email.value} or username: ${email.value} already exists.",
            )
        }
        val user = User.create(
            UUID.randomUUID().toString(),
            email.value,
            firstName?.value ?: "",
            lastName?.value ?: "",
        )
        users[email.value] = user
        return user
    }

    private fun checkIfUserExist(email: Email): Boolean = users.containsKey(email.value)
}
