package de.tabmates.server.groups.infra.database.entities

import de.tabmates.server.common.domain.type.TabEntryId
import de.tabmates.server.common.domain.type.TabEntrySplitId
import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.groups.infra.database.entities.types.SplitType
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
import java.math.BigDecimal

/**
 * Represents how a single participant is involved in splitting an expense.
 *
 * Each tab entry can have multiple splits (one per participant involved in the expense).
 * The split type determines how the [value] field is interpreted.
 *
 * Example scenarios:
 * - EQUAL split: 3 people split €30 equally → each owes €10 (value is ignored)
 * - EXACT_AMOUNT: Alice pays €20, Bob pays €10 → value = 20.00 for Alice, 10.00 for Bob
 * - PERCENTAGE: Alice pays 60%, Bob pays 40% → value = 60 for Alice, 40 for Bob
 * - SHARES: Alice has 2 shares, Bob has 1 share of €30 → Alice owes €20, Bob owes €10
 */
@Entity
@Table(
    name = "tab_entry_splits",
    schema = "group_service",
    indexes = [
        Index(
            name = "idx_tab_entry_split_tab_entry_id",
            columnList = "tab_entry_id",
        ),
        Index(
            name = "idx_tab_entry_split_participant_id",
            columnList = "participant_id",
        ),
    ],
)
class TabEntrySplitEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: TabEntrySplitId? = null,
    @Column(name = "tab_entry_id", nullable = false)
    var tabEntryId: TabEntryId,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "tab_entry_id",
        nullable = false,
        insertable = false,
        updatable = false,
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    var tabEntry: TabEntryEntity? = null,
    /**
     * The participant who owes money in this split.
     */
    @Column(name = "participant_id", nullable = false)
    var participantId: UserId,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "participant_id",
        nullable = false,
        insertable = false,
        updatable = false,
    )
    var participant: GroupParticipantEntity? = null,
    /**
     * The type of split calculation used.
     * All splits for a single tab entry should have the same split type.
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var splitType: SplitType,
    /**
     * The value used for split calculation. Interpretation depends on [splitType]:
     * - EQUAL: ignored (can be null or 0)
     * - EXACT_AMOUNT: the exact amount this participant owes
     * - PERCENTAGE: the percentage (0-100) this participant pays
     * - SHARES: the number of shares this participant has
     */
    @Column(nullable = false, precision = 19, scale = 4)
    var value: BigDecimal,
    /**
     * The computed actual amount this participant owes after applying the split calculation.
     * This is pre-calculated and stored for query efficiency.
     * For EXACT_AMOUNT, this equals [value].
     * For EQUAL, this is totalAmount / numberOfParticipants.
     * For PERCENTAGE, this is totalAmount * (value / 100).
     * For SHARES, this is totalAmount * (value / totalShares).
     */
    @Column(nullable = false, precision = 19, scale = 4)
    var resolvedAmount: BigDecimal,
)
