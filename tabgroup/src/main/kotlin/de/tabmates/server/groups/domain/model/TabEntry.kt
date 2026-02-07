package de.tabmates.server.groups.domain.model

import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.TabEntryId
import de.tabmates.server.common.domain.type.TabEntrySplitId
import de.tabmates.server.common.domain.type.UserId
import java.math.BigDecimal
import java.time.Instant
import java.util.Currency

/**
 * Represents an expense entry within a group.
 */
data class TabEntry(
    val id: TabEntryId,
    val groupId: GroupId,
    val creator: GroupParticipant,
    val paidBy: GroupParticipant,
    val title: String,
    val description: String,
    val amount: BigDecimal,
    val currency: Currency,
    val splits: List<TabEntrySplit>,
    val createdAt: Instant,
    val lastModifiedAt: Instant,
    val lastModifiedBy: GroupParticipant,
    val version: Int,
    val deletedAt: Instant?,
    val deletedBy: GroupParticipant?,
) {
    val isDeleted: Boolean
        get() = deletedAt != null
}

/**
 * Defines how an expense is split among participants.
 * Each subtype carries its associated value, making the relationship explicit and type-safe.
 */
sealed class Split {
    /**
     * Split equally among all participants.
     * Each participant pays totalAmount / numberOfParticipants.
     */
    data object Equal : Split()

    /**
     * Each participant pays an exact amount.
     * Sum of all exact amounts should equal the total expense amount.
     * @param amount the exact amount this participant owes
     */
    data class ExactAmount(val amount: BigDecimal) : Split()

    /**
     * Each participant pays a percentage of the total.
     * Sum of all percentages should equal 100.
     * @param percentage the percentage (0-100) this participant pays
     */
    data class Percentage(val percentage: BigDecimal) : Split()

    /**
     * Split by shares/parts (e.g., 2 shares vs 1 share means 2/3 vs 1/3).
     * Each participant pays (theirShares / totalShares) * totalAmount.
     * @param shares the number of shares for this participant
     */
    data class Shares(val shares: BigDecimal) : Split()
}

/**
 * Represents a single participant's share in an expense split.
 */
data class TabEntrySplit(
    val id: TabEntrySplitId,
    val participantId: UserId,
    val participant: GroupParticipant?,
    val split: Split,
    val resolvedAmount: BigDecimal,
)
