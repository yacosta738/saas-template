package com.loomify.engine.workspace.application

import com.loomify.common.domain.bus.query.Response
import com.loomify.engine.workspace.domain.Workspace

/**
 * Represents a workspace response.
 */
data class WorkspaceResponse(
    val id: String,
    val name: String,
    val description: String?,
    val ownerId: String,
    val createdAt: String,
    val updatedAt: String?
) : Response {
    companion object {
        fun from(workspace: Workspace) = WorkspaceResponse(
            id = workspace.id.value.toString(),
            name = workspace.name,
            description = workspace.description,
            ownerId = workspace.ownerId.value.toString(),
            createdAt = workspace.createdAt.toString(),
            updatedAt = workspace.updatedAt?.toString(),
        )
    }
}

/**
 * Represents a list of workspace responses.
 */
data class WorkspaceResponses(val data: List<WorkspaceResponse>) : Response {
    companion object {
        fun from(workspaces: List<Workspace>) = WorkspaceResponses(
            data = workspaces.map { WorkspaceResponse.from(it) },
        )
    }
}
