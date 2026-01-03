package de.tabmates.server.user.infra.database.repositories

import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.user.infra.database.entities.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, Long> {
    fun findByUserIdAndHashedToken(
        userId: UserId,
        hashedToken: String,
    ): RefreshTokenEntity?

    fun deleteByUserIdAndHashedToken(
        userId: UserId,
        hashedToken: String,
    )

    fun deleteByUserId(userId: UserId)
}
