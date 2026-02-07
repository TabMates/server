package de.tabmates.server.groups.infra.database.entities

import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.TabEntryHistoryId
import de.tabmates.server.common.domain.type.TabEntryId
import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.groups.infra.database.entities.types.ChangeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.Currency

/**
 * Stores a full snapshot of a [TabEntryEntity] at a specific point in time.
 *
 * This table provides a complete audit trail of all changes made to tab entries.
 * Each row represents the state of a tab entry after a specific change.
 *
 * Use cases:
 * - View the history of changes for a specific tab entry
 * - See what a tab entry looked like at any point in time
 * - Identify who made changes and when
 * - Support undo/restore functionality if needed
 */
@Entity
@Table(
    name = "tab_entry_history",
    schema = "group_service",
    indexes = [
        Index(
            name = "idx_tab_entry_history_entry_version",
            columnList = "tab_entry_id,version DESC",
        ),
        Index(
            name = "idx_tab_entry_history_group_changed_at",
            columnList = "group_id,changed_at DESC",
        ),
    ],
)
class TabEntryHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: TabEntryHistoryId? = null,
    /**
     * Reference to the original tab entry (may be soft-deleted).
     */
    @Column(name = "tab_entry_id", nullable = false)
    var tabEntryId: TabEntryId,
    /**
     * The group this entry belongs to.
     * Denormalized for efficient querying of group-wide history.
     */
    @Column(name = "group_id", nullable = false)
    var groupId: GroupId,
    /**
     * Version number of the tab entry at this snapshot.
     * Starts at 1 for creation, increments with each change.
     */
    @Column(nullable = false)
    var version: Int,
    /**
     * Type of change that resulted in this snapshot.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var changeType: ChangeType,
    /**
     * When this change was made.
     */
    @Column(name = "changed_at", nullable = false)
    var changedAt: Instant,
    /**
     * User who made this change.
     */
    @Column(name = "changed_by_user_id", nullable = false)
    var changedByUserId: UserId,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "changed_by_user_id",
        nullable = false,
        insertable = false,
        updatable = false,
    )
    var changedBy: GroupParticipantEntity? = null,
    @Column(nullable = false)
    var title: String,
    @Lob
    @Column(nullable = false, length = 256)
    var description: String,
    @Column(nullable = false, precision = 19, scale = 4)
    var amount: BigDecimal,
    @Column(nullable = false, length = 3)
    @Convert(converter = CurrencyConverter::class)
    var currency: Currency,
    @Column(name = "paid_by_user_id", nullable = false)
    var paidByUserId: UserId,
    /**
     * Original creator of the tab entry.
     */
    @Column(name = "creator_id", nullable = false)
    var creatorId: UserId,
    /**
     * When the tab entry was originally created.
     */
    @Column(name = "original_created_at", nullable = false)
    var originalCreatedAt: Instant,
    /**
     * JSON representation of the splits at this point in time.
     * Format: [{"participantId": "uuid", "splitType": "EQUAL", "value": 0, "resolvedAmount": 10.00}, ...]
     *
     * Stored as JSON because:
     * 1. Splits are rarely queried individually in historical context
     * 2. Simplifies storage and retrieval of historical state
     * 3. Avoids complex join queries for history display
     */
    @Lob
    @Column(name = "splits_snapshot", nullable = false, length = 4096)
    var splitsSnapshot: String,
)
