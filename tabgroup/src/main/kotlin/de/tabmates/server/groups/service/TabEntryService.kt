package de.tabmates.server.groups.service

import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.common.domain.type.TabEntryId
import de.tabmates.server.common.domain.type.UserId
import de.tabmates.server.groups.domain.exception.GroupNotFoundException
import de.tabmates.server.groups.domain.exception.GroupParticipantNotFoundException
import de.tabmates.server.groups.domain.exception.TabEntryNotFoundException
import de.tabmates.server.groups.domain.model.ChangeType
import de.tabmates.server.groups.domain.model.TabEntry
import de.tabmates.server.groups.domain.model.TabEntryHistory
import de.tabmates.server.groups.domain.model.TabEntrySplit
import de.tabmates.server.groups.infra.database.entities.TabEntryEntity
import de.tabmates.server.groups.infra.database.entities.TabEntryHistoryEntity
import de.tabmates.server.groups.infra.database.entities.TabEntrySplitEntity
import de.tabmates.server.groups.infra.database.entities.types.ChangeTypeDatabase
import de.tabmates.server.groups.infra.database.entities.types.SplitType
import de.tabmates.server.groups.infra.database.mappers.toChangeTypeDatabase
import de.tabmates.server.groups.infra.database.mappers.toGroupParticipant
import de.tabmates.server.groups.infra.database.mappers.toSplit
import de.tabmates.server.groups.infra.database.mappers.toSplitType
import de.tabmates.server.groups.infra.database.mappers.toTabEntry
import de.tabmates.server.groups.infra.database.mappers.toTabEntryHistory
import de.tabmates.server.groups.infra.database.mappers.toValue
import de.tabmates.server.groups.infra.database.repositories.GroupParticipantRepository
import de.tabmates.server.groups.infra.database.repositories.GroupRepository
import de.tabmates.server.groups.infra.database.repositories.TabEntryHistoryRepository
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
    private val tabEntryHistoryRepository: TabEntryHistoryRepository,
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

        val savedTabEntry = tabEntryRepository.save(tabEntry)

        saveHistory(savedTabEntry, ChangeType.CREATED, creatorId)

        return savedTabEntry.toTabEntry()
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

        val savedTabEntry = tabEntryRepository.save(existingTabEntry)

        saveHistory(savedTabEntry, ChangeType.UPDATED, modifiedByUserId)

        return savedTabEntry.toTabEntry()
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

        val savedTabEntry = tabEntryRepository.save(existingTabEntry)

        saveHistory(savedTabEntry, ChangeType.DELETED, deletedByUserId)
    }

    @Deprecated(message = "This was just the first version, the new method should be pageable.")
    fun getTabEntryHistory(tabEntryId: TabEntryId): List<TabEntryHistory> {
        tabEntryRepository.findByIdOrNull(tabEntryId)
            ?: throw TabEntryNotFoundException()

        val historyEntities =
            tabEntryHistoryRepository.findByTabEntryId(tabEntryId)

        return historyEntities.map { historyEntity ->
            val changedByParticipant =
                groupParticipantRepository
                    .findByIdOrNull(historyEntity.changedByUserId)
                    ?.toGroupParticipant()
                    ?: throw GroupParticipantNotFoundException(historyEntity.changedByUserId)

            val creator =
                groupParticipantRepository
                    .findByIdOrNull(historyEntity.creatorId)
                    ?.toGroupParticipant()
                    ?: throw GroupParticipantNotFoundException(historyEntity.creatorId)

            val paidBy =
                groupParticipantRepository
                    .findByIdOrNull(historyEntity.paidByUserId)
                    ?.toGroupParticipant()
                    ?: throw GroupParticipantNotFoundException(historyEntity.paidByUserId)

            val splitSnapshots = parseSplitsSnapshot(historyEntity.splitsSnapshot)
            val splits =
                splitSnapshots.map { snapshot ->
                    val participant =
                        groupParticipantRepository
                            .findByIdOrNull(UUID.fromString(snapshot.participantId))
                            ?.toGroupParticipant()

                    TabEntrySplit(
                        id = UUID.randomUUID(), // History splits don't have persistent IDs
                        participantId = UUID.fromString(snapshot.participantId),
                        participant = participant,
                        split = SplitType.valueOf(snapshot.splitType).toSplit(snapshot.value),
                        resolvedAmount = snapshot.resolvedAmount,
                    )
                }

            val deletedBy =
                if (historyEntity.changeType == ChangeTypeDatabase.DELETED) {
                    changedByParticipant
                } else {
                    null
                }

            historyEntity.toTabEntryHistory(
                changedByParticipant = changedByParticipant,
                creator = creator,
                paidBy = paidBy,
                splits = splits,
                deletedBy = deletedBy,
            )
        }
    }

    private fun saveHistory(
        tabEntry: TabEntryEntity,
        changeType: ChangeType,
        changedByUserId: UserId,
    ) {
        val historyEntity =
            TabEntryHistoryEntity(
                tabEntryId = tabEntry.id!!,
                groupId = tabEntry.groupId,
                version = tabEntry.version,
                changeType = changeType.toChangeTypeDatabase(),
                changedAt = Instant.now(),
                changedByUserId = changedByUserId,
                title = tabEntry.title,
                description = tabEntry.description,
                amount = tabEntry.amount,
                currency = tabEntry.currency,
                paidByUserId = tabEntry.paidByUserId,
                creatorId = tabEntry.creatorId,
                originalCreatedAt = tabEntry.createdAt,
                splitsSnapshot = tabEntry.splits.toSplitsSnapshotJson(),
            )

        tabEntryHistoryRepository.save(historyEntity)
    }

    private fun parseSplitsSnapshot(json: String): List<SplitSnapshotData> {
        if (json.isBlank() || json == "[]") return emptyList()

        val objectMapper =
            tools.jackson.databind.json.JsonMapper
                .builder()
                .addModule(
                    tools.jackson.module.kotlin
                        .kotlinModule(),
                ).build()
        return objectMapper.readValue(
            json,
            objectMapper.typeFactory.constructCollectionType(List::class.java, SplitSnapshotData::class.java),
        )
    }

    private fun List<TabEntrySplitEntity>.toSplitsSnapshotJson(): String {
        val snapshots =
            this.map { split ->
                mapOf(
                    "participantId" to split.participantId.toString(),
                    "splitType" to split.splitType.name,
                    "value" to split.value,
                    "resolvedAmount" to split.resolvedAmount,
                )
            }
        val objectMapper =
            tools.jackson.databind.json.JsonMapper
                .builder()
                .addModule(
                    tools.jackson.module.kotlin
                        .kotlinModule(),
                ).build()
        return objectMapper.writeValueAsString(snapshots)
    }
}

private data class SplitSnapshotData(
    val participantId: String,
    val splitType: String,
    val value: BigDecimal,
    val resolvedAmount: BigDecimal,
)
