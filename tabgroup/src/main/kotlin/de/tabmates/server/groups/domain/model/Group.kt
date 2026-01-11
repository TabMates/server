package de.tabmates.server.groups.domain.model

import de.tabmates.server.common.domain.type.GroupId
import java.time.Instant

data class Group(
    val id: GroupId,
    val participants: Set<GroupParticipant>,
    val creator: GroupParticipant,
    val lastActivityAt: Instant,
    val createdAt: Instant,
)
