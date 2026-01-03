package de.tabmates.server.user.infra.database.repositories

import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.user.infra.database.entities.RegisteredUserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RegisteredUserRepository : JpaRepository<RegisteredUserEntity, UserId> {
    fun findByEmail(email: String): RegisteredUserEntity?

    fun findByEmailOrUsername(
        email: String,
        username: String,
    ): RegisteredUserEntity?
}
