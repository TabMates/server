package de.tabmates.server.groups.api.dto.ws

import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.TabEntryId

data class DeleteTabEntryDto(
    val groupId: GroupId,
    val tabEntryId: TabEntryId,
)
