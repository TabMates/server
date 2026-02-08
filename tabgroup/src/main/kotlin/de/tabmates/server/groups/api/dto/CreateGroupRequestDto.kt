package de.tabmates.server.groups.api.dto

import de.tabmates.server.common.domain.type.UserId

data class CreateGroupRequestDto(
    val otherParticipantUserIds: List<UserId>,
)
