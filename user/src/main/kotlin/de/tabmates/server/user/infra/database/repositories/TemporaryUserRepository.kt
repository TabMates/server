package de.tabmates.server.user.infra.database.repositories

import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.user.infra.database.entities.RegisteredUserEntity
import de.tabmates.server.user.infra.database.entities.TemporaryUserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TemporaryUserRepository : JpaRepository<TemporaryUserEntity, UserId>
