package de.tabmates.server.groups.api.dto.ws

import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.TabEntryId
import de.tabmates.server.common.domain.type.TabEntrySplitId
import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.groups.api.dto.SplitDto
import de.tabmates.server.groups.api.dto.TabEntrySplitDto
import java.math.BigDecimal
import java.util.Currency

data class NewTabEntryDto(
    val id: TabEntryId? = null,
    val groupId: GroupId,
    val paidByUserId: UserId,
    val title: String,
    val description: String,
    val amount: BigDecimal,
    val currency: Currency,
    val splits: List<NewTabEntrySplitDto>,
)

data class NewTabEntrySplitDto(
    val id: TabEntrySplitId? = null,
    val participantId: UserId,
    val split: SplitDto,
    val resolvedAmount: BigDecimal,
)
