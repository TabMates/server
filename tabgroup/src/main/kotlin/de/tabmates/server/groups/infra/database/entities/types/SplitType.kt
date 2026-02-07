package de.tabmates.server.groups.infra.database.entities.types

/**
 * Defines how an expense is split among participants.
 * Mirrors functionality found in apps like Splitwise.
 */
enum class SplitType {
    /**
     * Split equally among all participants.
     * The [de.tabmates.server.groups.infra.database.entities.TabEntrySplitEntity.value] field is ignored; each participant pays totalAmount / numberOfParticipants.
     */
    EQUAL,

    /**
     * Each participant pays an exact amount.
     * The [de.tabmates.server.groups.infra.database.entities.TabEntrySplitEntity.value] field contains the exact amount this participant owes.
     * Sum of all exact amounts should equal the total expense amount.
     */
    EXACT_AMOUNT,

    /**
     * Each participant pays a percentage of the total.
     * The [de.tabmates.server.groups.infra.database.entities.TabEntrySplitEntity.value] field contains the percentage (0-100).
     * Sum of all percentages should equal 100.
     */
    PERCENTAGE,

    /**
     * Split by shares/parts (e.g., 2 shares vs 1 share means 2/3 vs 1/3).
     * The [de.tabmates.server.groups.infra.database.entities.TabEntrySplitEntity.value] field contains the number of shares for this participant.
     * Each participant pays (theirShares / totalShares) * totalAmount.
     */
    SHARES,
}
