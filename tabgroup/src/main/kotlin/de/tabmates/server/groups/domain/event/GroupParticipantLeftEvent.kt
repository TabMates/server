package de.tabmates.server.groups.domain.event

import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.UserId

data class GroupParticipantLeftEvent(
    val groupId: GroupId,
    val userId: UserId,
)
