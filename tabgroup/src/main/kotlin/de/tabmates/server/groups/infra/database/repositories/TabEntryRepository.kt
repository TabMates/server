package de.tabmates.server.groups.infra.database.repositories

import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.TabEntryId
import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.groups.infra.database.entities.GroupEntity
import de.tabmates.server.groups.infra.database.entities.TabEntryEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface TabEntryRepository : JpaRepository<TabEntryEntity, TabEntryId> {
    @Query(
        """
        SELECT te
        FROM TabEntryEntity te
        WHERE te.groupId = :groupId
        AND te.createdAt < :before
        ORDER BY te.createdAt DESC
    """,
    )
    fun findByTabEntryIdBefore(
        groupId: GroupId,
        before: Instant,
        pageable: Pageable,
    ): Slice<TabEntryEntity>

    @Query(
        """
        SELECT te
        FROM TabEntryEntity te
        LEFT JOIN FETCH te.creator
        WHERE te.groupId IN :groupIds
            AND (te.createdAt, te.id) = (
                SELECT te2.createdAt, te2.id
                FROM TabEntryEntity te2
                WHERE te2.groupId = te.groupId
                ORDER BY te2.createdAt DESC
                LIMIT 1
            )
    """,
    )
    fun findLatestTabEntryByGroupIds(groupIds: Set<GroupId>): List<TabEntryEntity>
}
