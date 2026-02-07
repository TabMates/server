package de.tabmates.server.groups.infra.database.repositories

import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.groups.infra.database.entities.GroupParticipantEntity
import org.springframework.data.jpa.repository.JpaRepository

interface GroupParticipantRepository : JpaRepository<GroupParticipantEntity, UserId> {
    fun findByUserIdIn(userIds: Set<UserId>): Set<GroupParticipantEntity>
}
