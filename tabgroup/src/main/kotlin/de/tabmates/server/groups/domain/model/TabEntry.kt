package de.tabmates.server.groups.domain.model

import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.TabEntryId
import java.time.Instant
import java.util.Currency

data class TabEntry(
    val id: TabEntryId,
    val groupId: GroupId,
    val creator: GroupParticipant,
    val title: String,
    val description: String,
    val amount: Double,
    val currency: Currency,
    val createdAt: Instant,
)
