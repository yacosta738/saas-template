package com.loomify.common.domain.error

abstract class EntityNotFoundException(
    override val message: String,
    override val cause: Throwable? = null
) : BusinessRuleValidationException(message, cause)
