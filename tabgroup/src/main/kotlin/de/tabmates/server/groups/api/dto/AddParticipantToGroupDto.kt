package de.tabmates.server.groups.api.dto

import de.tabmates.server.common.domain.type.UserId
import jakarta.validation.constraints.Size

data class AddParticipantToGroupDto(
    @field:Size(min = 1)
    val userIds: List<UserId>,
)
