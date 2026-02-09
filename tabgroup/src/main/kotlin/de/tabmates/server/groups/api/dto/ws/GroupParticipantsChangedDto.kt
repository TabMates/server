package de.tabmates.server.groups.api.dto.ws

import de.tabmates.server.common.domain.type.GroupId

data class GroupParticipantsChangedDto(
    val groupId: GroupId,
)
