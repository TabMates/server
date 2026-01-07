package de.tabmates.server.user.infra.database.entities

import de.tabmates.server.common.domain.type.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(
    name = "users_temporary",
    schema = "user_service",
    indexes = [
        Index(name = "idx_users_id", columnList = "id"),
    ],
)
class TemporaryUserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UserId? = null,
    @Column(nullable = false)
    var username: String,
    @Column(nullable = false)
    var hashedPassword: String,
    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
    @UpdateTimestamp
    var updatedAt: Instant = Instant.now(),
)
