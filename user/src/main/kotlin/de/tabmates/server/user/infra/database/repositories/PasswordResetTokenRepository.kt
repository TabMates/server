package de.tabmates.server.user.infra.database.repositories

import de.tabmates.server.user.infra.database.entities.PasswordResetTokenEntity
import de.tabmates.server.user.infra.database.entities.RegisteredUserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface PasswordResetTokenRepository : JpaRepository<PasswordResetTokenEntity, Long> {
    fun findByToken(token: String): PasswordResetTokenEntity?

    fun deleteByExpiresAtLessThan(now: Instant)

    @Modifying
    @Query(
        """
        UPDATE PasswordResetTokenEntity p
        SET p.usedAt = CURRENT_TIMESTAMP
        WHERE p.user = :user AND p.usedAt IS NULL
    """,
    )
    fun invalidateActiveTokensForUser(user: RegisteredUserEntity)
}
