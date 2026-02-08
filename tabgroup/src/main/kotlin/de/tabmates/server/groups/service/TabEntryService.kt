package de.tabmates.server.groups.service

import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.TabEntryId
import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.groups.domain.exception.GroupNotFoundException
import de.tabmates.server.groups.domain.exception.GroupParticipantNotFoundException
import de.tabmates.server.groups.domain.exception.TabEntryNotFoundException
import de.tabmates.server.groups.domain.model.TabEntry
import de.tabmates.server.groups.domain.model.TabEntrySplit
import de.tabmates.server.groups.infra.database.entities.TabEntryEntity
import de.tabmates.server.groups.infra.database.entities.TabEntrySplitEntity
import de.tabmates.server.groups.infra.database.mappers.toSplitType
import de.tabmates.server.groups.infra.database.mappers.toTabEntry
import de.tabmates.server.groups.infra.database.mappers.toValue
import de.tabmates.server.groups.infra.database.repositories.GroupParticipantRepository
import de.tabmates.server.groups.infra.database.repositories.GroupRepository
import de.tabmates.server.groups.infra.database.repositories.TabEntryRepository
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.util.Currency
import java.util.UUID

@Service
class TabEntryService(
    private val groupRepository: GroupRepository,
    private val groupParticipantRepository: GroupParticipantRepository,
    private val tabEntryRepository: TabEntryRepository,
) {
    @Transactional
    fun addTabEntry(
        groupId: GroupId,
        creatorId: UserId,
        paidByUserId: UserId,
        title: String,
        description: String,
        amount: BigDecimal,
        currency: Currency,
        splits: List<TabEntrySplit>,
        tabEntryId: TabEntryId? = null,
    ): TabEntry {
        val group =
            groupRepository.findGroupById(groupId, creatorId)
                ?: throw GroupNotFoundException()
        val creator =
            groupParticipantRepository.findByIdOrNull(creatorId)
                ?: throw GroupParticipantNotFoundException(creatorId)
        val paidByUser =
            groupParticipantRepository.findByIdOrNull(paidByUserId)
                ?: throw GroupParticipantNotFoundException(paidByUserId)
        val newTabEntryId = tabEntryId ?: UUID.randomUUID()

        val tabEntrySplitEntities =
            splits
                .map { split ->
                    val participant =
                        groupParticipantRepository.findByIdOrNull(split.participantId)
                            ?: throw GroupParticipantNotFoundException(split.participantId)

                    TabEntrySplitEntity(
                        tabEntryId = newTabEntryId,
                        participantId = split.participantId,
                        participant = participant,
                        splitType = split.split.toSplitType(),
                        value = split.split.toValue(),
                        resolvedAmount = split.resolvedAmount,
                    )
                }.toMutableList()

        val tabEntry =
            TabEntryEntity(
                id = newTabEntryId,
                title = title,
                description = description,
                amount = amount,
                currency = currency,
                groupId = groupId,
                group = group,
                creatorId = creatorId,
                creator = creator,
                paidByUserId = paidByUserId,
                paidBy = paidByUser,
                splits = tabEntrySplitEntities,
                lastModifiedByUserId = creatorId,
                lastModifiedBy = creator,
            )

        return tabEntryRepository.save(tabEntry).toTabEntry()
    }

    @Transactional
    fun updateTabEntry(
        tabEntryId: TabEntryId,
        modifiedByUserId: UserId,
        paidByUserId: UserId? = null,
        title: String? = null,
        description: String? = null,
        amount: BigDecimal? = null,
        currency: Currency? = null,
        splits: List<TabEntrySplit>? = null,
    ): TabEntry {
        val existingTabEntry =
            tabEntryRepository.findByIdOrNull(tabEntryId)
                ?: throw TabEntryNotFoundException()

        if (existingTabEntry.isDeleted) {
            throw TabEntryNotFoundException()
        }

        val modifiedByUser =
            groupParticipantRepository.findByIdOrNull(modifiedByUserId)
                ?: throw GroupParticipantNotFoundException(modifiedByUserId)

        // Update basic fields if provided
        title?.let { existingTabEntry.title = it }
        description?.let { existingTabEntry.description = it }
        amount?.let { existingTabEntry.amount = it }
        currency?.let { existingTabEntry.currency = it }

        // Update paidBy if provided
        paidByUserId?.let { userId ->
            val paidByUser =
                groupParticipantRepository.findByIdOrNull(userId)
                    ?: throw GroupParticipantNotFoundException(userId)
            existingTabEntry.paidByUserId = userId
            existingTabEntry.paidBy = paidByUser
        }

        // Update splits if provided
        splits?.let { newSplits ->
            // Clear existing splits (orphanRemoval will delete them)
            existingTabEntry.splits.clear()

            // Add new splits
            val tabEntrySplitEntities =
                newSplits.map { split ->
                    val participant =
                        groupParticipantRepository.findByIdOrNull(split.participantId)
                            ?: throw GroupParticipantNotFoundException(split.participantId)

                    TabEntrySplitEntity(
                        tabEntryId = tabEntryId,
                        participantId = split.participantId,
                        participant = participant,
                        splitType = split.split.toSplitType(),
                        value = split.split.toValue(),
                        resolvedAmount = split.resolvedAmount,
                    )
                }
            existingTabEntry.splits.addAll(tabEntrySplitEntities)
        }

        // Update audit fields
        existingTabEntry.lastModifiedByUserId = modifiedByUserId
        existingTabEntry.lastModifiedBy = modifiedByUser

        return tabEntryRepository.save(existingTabEntry).toTabEntry()
    }

    @Transactional
    fun deleteTabEntry(
        tabEntryId: TabEntryId,
        deletedByUserId: UserId,
    ) {
        val existingTabEntry =
            tabEntryRepository.findByIdOrNull(tabEntryId)
                ?: throw TabEntryNotFoundException()

        if (existingTabEntry.isDeleted) {
            throw TabEntryNotFoundException()
        }

        val deletedByUser =
            groupParticipantRepository.findByIdOrNull(deletedByUserId)
                ?: throw GroupParticipantNotFoundException(deletedByUserId)

        existingTabEntry.deletedAt = Instant.now()
        existingTabEntry.deletedByUserId = deletedByUserId
        existingTabEntry.deletedBy = deletedByUser
        existingTabEntry.lastModifiedByUserId = deletedByUserId
        existingTabEntry.lastModifiedBy = deletedByUser

        tabEntryRepository.save(existingTabEntry)
    }
}
