package de.tabmates.server.groups.domain.exception

import de.tabmates.server.common.domain.type.UserId

class GroupParticipantNotFoundException(
    id: UserId,
) : RuntimeException(
        "The group participant with the ID $id was not found.",
    )
