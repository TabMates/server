package de.tabmates.server.groups.infra.database.entities

import de.tabmates.server.common.domain.type.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

enum class UserTypeDatabase {
    REGISTERED,
    ANONYMOUS,
    PLACEHOLDER,
}

@Entity
@Table(
    name = "group_participants",
    schema = "group_service",
    indexes = [
        Index(name = "idx_group_participant_username", columnList = "username"),
    ],
)
class GroupParticipantEntity(
    @Id
    var userId: UserId,
    @Column(nullable = false, unique = false)
    var username: String,
    @Column(nullable = true, unique = false)
    var email: String?,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var userType: UserTypeDatabase,
    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
)
