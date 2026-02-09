package de.tabmates.server.groups.api.mappers

import de.tabmates.server.groups.api.dto.ws.NewTabEntrySplitDto
import de.tabmates.server.groups.domain.model.TabEntrySplit

fun NewTabEntrySplitDto.toTabEntry(): TabEntrySplit {
    return TabEntrySplit(
        id = this.id,
        participantId = this.participantId,
        split = split.toSplit(),
        participant = null,
        resolvedAmount = this.resolvedAmount,
    )
}
