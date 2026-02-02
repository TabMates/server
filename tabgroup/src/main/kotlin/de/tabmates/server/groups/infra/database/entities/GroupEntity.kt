package de.tabmates.server.groups.infra.database.entities

import de.tabmates.server.common.domain.type.GroupId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "groups",
    schema = "group_service",
)
class GroupEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: GroupId? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    var creator: GroupParticipantEntity,
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "group_participants_cross_ref",
        schema = "group_service",
        joinColumns = [JoinColumn(name = "group_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")],
        indexes = [
            // Answers efficiently: Who is in the group X?
            Index(
                name = "idx_group_participant_group_id_user_id",
                columnList = "group_id,user_id",
                unique = true,
            ),
            // Answers efficiently: What group is user X in?
            Index(
                name = "idx_group_participant_user_id_group_id",
                columnList = "user_id,group_id",
                unique = true,
            ),
        ],
    )
    var participants: Set<GroupParticipantEntity> = emptySet(),
    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
)
