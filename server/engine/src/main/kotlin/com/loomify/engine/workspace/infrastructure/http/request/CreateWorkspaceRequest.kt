package com.loomify.engine.workspace.infrastructure.http.request

import jakarta.validation.constraints.NotBlank

/**
 * This data class represents the request body for creating a workspace.
 *
 * @property name The name of the workspace to be created. This field is mandatory.
 * @property description An optional description of the workspace. This field is not mandatory.
 * @property ownerId The ID of the user who is creating the workspace. This field is mandatory.
 */
data class CreateWorkspaceRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,
    val description: String? = null,
    @field:NotBlank(message = "User Id is required")
    val ownerId: String,
)
