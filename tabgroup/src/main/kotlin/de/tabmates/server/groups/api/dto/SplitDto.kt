package de.tabmates.server.groups.api.dto

import java.math.BigDecimal

sealed class SplitDto {
    /**
     * Split equally among all participants.
     * Each participant pays totalAmount / numberOfParticipants.
     */
    data object Equal : SplitDto()

    /**
     * Each participant pays an exact amount.
     * Sum of all exact amounts should equal the total expense amount.
     * @param amount the exact amount this participant owes
     */
    data class ExactAmount(val amount: BigDecimal) : SplitDto()

    /**
     * Each participant pays a percentage of the total.
     * Sum of all percentages should equal 100.
     * @param percentage the percentage (0-100) this participant pays
     */
    data class Percentage(val percentage: BigDecimal) : SplitDto()

    /**
     * Split by shares/parts (e.g., 2 shares vs 1 share means 2/3 vs 1/3).
     * Each participant pays (theirShares / totalShares) * totalAmount.
     * @param shares the number of shares for this participant
     */
    data class Shares(val shares: BigDecimal) : SplitDto()
}
