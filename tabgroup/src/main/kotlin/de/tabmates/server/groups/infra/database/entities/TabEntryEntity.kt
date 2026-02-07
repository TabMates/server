package de.tabmates.server.groups.infra.database.entities

import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.TabEntryId
import de.tabmates.server.common.domain.type.UserId
import jakarta.persistence.AttributeConverter
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.Instant
import java.util.Currency

/**
 * Represents an expense entry within a group.
 *
 * This entity stores the current state of an expense. Historical changes
 * are tracked in [TabEntryHistoryEntity].
 *
 * Key features:
 * - Soft delete support via [deletedAt] field
 * - Optimistic locking via [version] field
 * - Flexible expense splitting via [splits] relationship
 * - Full audit trail with [lastModifiedAt] and [lastModifiedByUserId]
 */
@Entity
@Table(
    name = "tab_entries",
    schema = "group_service",
    indexes = [
        Index(
            name = "idx_tab_entry_group_id_created_at",
            columnList = "group_id,created_at DESC",
        ),
        Index(
            name = "idx_tab_entry_group_id_deleted_at",
            columnList = "group_id,deleted_at",
        ),
    ],
)
class TabEntryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: TabEntryId? = null,
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
    @Column(
        name = "group_id",
        nullable = false,
        updatable = false,
    )
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
    @Column(name = "creator_id", nullable = false, updatable = false)
    var creatorId: UserId,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "creator_id",
        nullable = false,
        insertable = false,
        updatable = false,
    )
    var creator: GroupParticipantEntity,
    @Column(name = "paid_by_user_id", nullable = false)
    var paidByUserId: UserId,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "paid_by_user_id",
        nullable = false,
        insertable = false,
        updatable = false,
    )
    var paidBy: GroupParticipantEntity,
    /**
     * How this expense is split among participants.
     * Each split defines how much each participant owes.
     */
    @OneToMany(
        mappedBy = "tabEntry",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER,
    )
    var splits: MutableList<TabEntrySplitEntity> = mutableListOf(),
    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
    @UpdateTimestamp
    var lastModifiedAt: Instant = Instant.now(),
    @Column(name = "last_modified_by_user_id", nullable = false)
    var lastModifiedByUserId: UserId,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "last_modified_by_user_id",
        nullable = false,
        insertable = false,
        updatable = false,
    )
    var lastModifiedBy: GroupParticipantEntity,
    /**
     * Version for optimistic locking and history tracking.
     * Incremented on each update.
     */
    @Version
    @Column(nullable = false)
    var version: Int = 1,
    /**
     * When this expense was deleted (soft delete).
     * Null means the expense is active.
     */
    @Column(nullable = true)
    var deletedAt: Instant? = null,
    /**
     * User who deleted this expense.
     * Null if not deleted.
     */
    @Column(name = "deleted_by_user_id", nullable = true)
    var deletedByUserId: UserId? = null,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "deleted_by_user_id",
        nullable = true,
        insertable = false,
        updatable = false,
    )
    var deletedBy: GroupParticipantEntity? = null,
) {
    val isDeleted: Boolean
        get() = deletedAt != null
}

@Converter(autoApply = false)
class CurrencyConverter : AttributeConverter<Currency, String> {
    override fun convertToDatabaseColumn(attribute: Currency?): String? = attribute?.currencyCode

    override fun convertToEntityAttribute(dbData: String?): Currency? = dbData?.let { Currency.getInstance(it) }
}
