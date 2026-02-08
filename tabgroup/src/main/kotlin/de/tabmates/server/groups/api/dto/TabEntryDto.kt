package de.tabmates.server.groups.api.dto

import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.TabEntryId
import java.math.BigDecimal
import java.time.Instant
import java.util.Currency

data class TabEntryDto(
    val id: TabEntryId,
    val groupId: GroupId,
    val creator: GroupParticipantDto,
    val paidBy: GroupParticipantDto,
    val title: String,
    val description: String,
    val amount: BigDecimal,
    val currency: Currency,
    val splits: List<TabEntrySplitDto>,
    val createdAt: Instant,
    val lastModifiedAt: Instant,
    val lastModifiedBy: GroupParticipantDto,
    val version: Int,
    val deletedAt: Instant?,
    val deletedBy: GroupParticipantDto?,
) {
    val isDeleted: Boolean
        get() = deletedAt != null
}
