package de.tabmates.server.groups.service

import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.groups.domain.model.GroupParticipant
import de.tabmates.server.groups.infra.database.mappers.toGroupParticipant
import de.tabmates.server.groups.infra.database.mappers.toGroupParticipantEntity
import de.tabmates.server.groups.infra.database.repositories.GroupParticipantRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class GroupParticipantService(
    private val groupParticipantRepository: GroupParticipantRepository,
) {
    fun createGroupParticipant(groupParticipant: GroupParticipant) {
        groupParticipantRepository.save(
            groupParticipant.toGroupParticipantEntity(),
        )
    }

    fun findGroupParticipantById(userId: UserId): GroupParticipant? {
        return groupParticipantRepository.findByIdOrNull(userId)?.toGroupParticipant()
    }
}
