package de.tabmates.server.groups.domain.model

import de.tabmates.server.common.domain.type.TabEntryHistoryId
import java.time.Instant

enum class ChangeType {
    CREATED,
    UPDATED,
    DELETED,
}

data class TabEntryHistory(
    val historyId: TabEntryHistoryId,
    val changeType: ChangeType,
    val changedAt: Instant,
    val changedBy: GroupParticipant,
    val tabEntry: TabEntry,
)
