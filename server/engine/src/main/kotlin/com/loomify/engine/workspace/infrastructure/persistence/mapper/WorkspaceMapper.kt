package com.loomify.engine.workspace.infrastructure.persistence.mapper

import com.loomify.engine.users.domain.UserId
import com.loomify.engine.workspace.domain.Workspace
import com.loomify.engine.workspace.domain.WorkspaceId
import com.loomify.engine.workspace.infrastructure.persistence.entity.WorkspaceEntity

/**
 * This object provides mapping functions to convert between domain and entity objects.
 */
object WorkspaceMapper {
    /**
     * Converts a [Workspace] domain object to a [WorkspaceEntity].
     *
     * @receiver The [Workspace] domain object to convert.
     * @return The converted [WorkspaceEntity].
     */
    fun Workspace.toEntity(): WorkspaceEntity = WorkspaceEntity(
        id = id.value,
        name = name,
        description = description,
        ownerId = ownerId.value,
        isDefault = isDefault,
        createdAt = createdAt,
        createdBy = createdBy,
        updatedAt = updatedAt,
        updatedBy = updatedBy,
    )

    /**
     * Converts a [WorkspaceEntity] to a [Workspace] domain object.
     *
     * @receiver The [WorkspaceEntity] to convert.
     * @return The converted [Workspace] domain object.
     */
    fun WorkspaceEntity.toDomain(): Workspace = Workspace(
        id = WorkspaceId(id),
        name = name,
        description = description,
        ownerId = UserId(ownerId),
        isDefault = isDefault,
        createdAt = createdAt,
        createdBy = createdBy,
        updatedAt = updatedAt,
        updatedBy = updatedBy,
    )
}
