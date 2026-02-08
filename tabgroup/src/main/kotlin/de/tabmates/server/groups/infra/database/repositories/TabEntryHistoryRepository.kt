package de.tabmates.server.groups.infra.database.repositories

import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.TabEntryHistoryId
import de.tabmates.server.common.domain.type.TabEntryId
import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.groups.infra.database.entities.GroupEntity
import de.tabmates.server.groups.infra.database.entities.TabEntryEntity
import de.tabmates.server.groups.infra.database.entities.TabEntryHistoryEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface TabEntryHistoryRepository : JpaRepository<TabEntryHistoryEntity, TabEntryHistoryId> {
    @Query(
        """
        SELECT te
        FROM TabEntryHistoryEntity te
        WHERE te.groupId = :groupId
        AND te.changedAt < :before
        ORDER BY te.changedAt DESC
    """,
    )
    fun findByGroupIdBefore(
        groupId: GroupId,
        before: Instant,
        pageable: Pageable,
    ): Slice<TabEntryHistoryEntity>

    @Query(
        """
        SELECT te
        FROM TabEntryHistoryEntity te
        WHERE te.tabEntryId = :tabEntryId
        ORDER BY te.version DESC
    """,
    )
    fun findByTabEntryId(tabEntryId: TabEntryId): List<TabEntryHistoryEntity>
}
