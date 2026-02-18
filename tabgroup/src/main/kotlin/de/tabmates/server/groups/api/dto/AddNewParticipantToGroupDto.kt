package de.tabmates.server.groups.api.dto

import jakarta.validation.constraints.Size

data class AddNewParticipantToGroupDto(
    @field:Size(min = 1)
    val usernames: List<String>,
)
