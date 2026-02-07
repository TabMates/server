package de.tabmates.server.groups.infra.database.repositories

import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.groups.infra.database.entities.GroupEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GroupRepository : JpaRepository<GroupEntity, GroupId> {
    @Query(
        """
        SELECT g
        FROM GroupEntity g
        LEFT JOIN FETCH g.participants
        LEFT JOIN FETCH g.creator
        WHERE g.id = :groupId
        AND EXISTS (
            SELECT 1
            FROM g.participants p
            WHERE p.userId = :userId
        )
    """,
    )
    fun findGroupById(
        groupId: GroupId,
        userId: UserId,
    ): GroupEntity?

    @Query(
        """
        SELECT g
        FROM GroupEntity g
        LEFT JOIN FETCH g.participants
        LEFT JOIN FETCH g.creator
        WHERE EXISTS (
            SELECT 1
            FROM g.participants p
            WHERE p.userId = :userId
        )
    """,
    )
    fun findAllByUserId(userId: UserId): List<GroupEntity>
}
