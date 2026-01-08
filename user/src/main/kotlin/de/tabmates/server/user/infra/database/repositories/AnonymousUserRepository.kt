package de.tabmates.server.user.infra.database.repositories

import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.user.infra.database.entities.AnonymousUserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AnonymousUserRepository : JpaRepository<AnonymousUserEntity, UserId>
