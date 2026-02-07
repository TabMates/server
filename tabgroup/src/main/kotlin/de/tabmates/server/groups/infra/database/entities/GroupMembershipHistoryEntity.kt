package de.tabmates.server.groups.infra.database.entities

import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.GroupMembershipHistoryId
import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.groups.infra.database.entities.types.MembershipChangeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.Instant

/**
 * Records membership changes within a group.
 *
 * This table tracks when users join or leave a group, providing a history
 * of membership changes that can be combined with [TabEntryHistoryEntity]
 * to create a complete activity feed on the client side.
 *
 * Use cases:
 * - Show when users joined or left the group in the activity feed
 * - Track who removed a user from the group
 * - Audit trail of group membership changes
 */
@Entity
@Table(
    name = "group_membership_history",
    schema = "group_service",
    indexes = [
        Index(
            name = "idx_membership_history_group_changed_at",
            columnList = "group_id,changed_at DESC",
        ),
        Index(
            name = "idx_membership_history_user_changed_at",
            columnList = "user_id,changed_at DESC",
        ),
    ],
)
class GroupMembershipHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: GroupMembershipHistoryId? = null,
    /**
     * The group where the membership change occurred.
     */
    @Column(name = "group_id", nullable = false)
    var groupId: GroupId,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "group_id",
        nullable = false,
        insertable = false,
        updatable = false,
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    var group: GroupEntity? = null,
    /**
     * The user whose membership changed (joined/left/removed).
     */
    @Column(name = "user_id", nullable = false)
    var userId: UserId,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        insertable = false,
        updatable = false,
    )
    var user: GroupParticipantEntity? = null,
    /**
     * Type of membership change.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var changeType: MembershipChangeType,
    /**
     * The user who performed the action.
     * - For JOINED: the user who joined (same as [userId]) or who invited them
     * - For LEFT: the user who left (same as [userId])
     * - For REMOVED: the user who removed the member
     */
    @Column(name = "performed_by_user_id", nullable = false)
    var performedByUserId: UserId,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "performed_by_user_id",
        nullable = false,
        insertable = false,
        updatable = false,
    )
    var performedBy: GroupParticipantEntity? = null,
    /**
     * When this membership change occurred.
     */
    @Column(name = "changed_at", nullable = false)
    var changedAt: Instant,
)
