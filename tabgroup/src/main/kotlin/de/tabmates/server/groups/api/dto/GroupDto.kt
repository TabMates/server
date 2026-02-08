package de.tabmates.server.groups.api.dto

import de.tabmates.server.common.domain.type.GroupId
import java.time.Instant

data class GroupDto(
    val id: GroupId,
    val participants: Set<GroupParticipantDto>,
    val creator: GroupParticipantDto,
    val lastActivityAt: Instant,
    val createdAt: Instant,
)
