package de.tabmates.server.groups.api.dto

import de.tabmates.server.common.domain.type.UserId

data class GroupParticipantDto(
    val userId: UserId,
    val username: String,
    val email: String?,
    val userType: UserTypeDto,
)
