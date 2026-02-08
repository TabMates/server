package de.tabmates.server.groups.api.dto

import de.tabmates.server.common.domain.type.TabEntrySplitId
import de.tabmates.server.common.domain.type.UserId
import java.math.BigDecimal

data class TabEntrySplitDto(
    val id: TabEntrySplitId,
    val participantId: UserId,
    val participant: GroupParticipantDto?,
    val split: SplitDto,
    val resolvedAmount: BigDecimal,
)
