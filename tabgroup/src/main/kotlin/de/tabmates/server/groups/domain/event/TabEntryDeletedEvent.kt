package de.tabmates.server.groups.domain.event

import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.TabEntryId

data class TabEntryDeletedEvent(
    val groupId: GroupId,
    val tabEntryId: TabEntryId,
)
