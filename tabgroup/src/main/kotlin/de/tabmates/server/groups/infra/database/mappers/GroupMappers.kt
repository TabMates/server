package de.tabmates.server.groups.infra.database.mappers

import de.tabmates.server.groups.domain.model.ChangeType
import de.tabmates.server.groups.domain.model.Group
import de.tabmates.server.groups.domain.model.GroupParticipant
import de.tabmates.server.groups.domain.model.Split
import de.tabmates.server.groups.domain.model.TabEntry
import de.tabmates.server.groups.domain.model.TabEntryHistory
import de.tabmates.server.groups.domain.model.TabEntrySplit
import de.tabmates.server.groups.domain.model.UserType
import de.tabmates.server.groups.infra.database.entities.GroupEntity
import de.tabmates.server.groups.infra.database.entities.GroupParticipantEntity
import de.tabmates.server.groups.infra.database.entities.TabEntryEntity
import de.tabmates.server.groups.infra.database.entities.TabEntryHistoryEntity
import de.tabmates.server.groups.infra.database.entities.TabEntrySplitEntity
import de.tabmates.server.groups.infra.database.entities.UserTypeDatabase
import de.tabmates.server.groups.infra.database.entities.types.ChangeTypeDatabase
import de.tabmates.server.groups.infra.database.entities.types.SplitType
import java.math.BigDecimal

fun GroupEntity.toGroup(lastTabEntry: TabEntry? = null): Group {
    return Group(
        id = this.id!!,
        participants =
            this.participants
                .map {
                    it.toGroupParticipant()
                }.toSet(),
        creator = this.creator.toGroupParticipant(),
        lastActivityAt = lastTabEntry?.createdAt ?: createdAt,
        createdAt = this.createdAt,
    )
}

fun GroupParticipantEntity.toGroupParticipant(): GroupParticipant {
    return GroupParticipant(
        userId = userId,
        username = username,
        email = email,
        userType = userType.toUserType(),
    )
}

fun GroupParticipant.toGroupParticipantEntity(): GroupParticipantEntity {
    return GroupParticipantEntity(
        userId = userId,
        username = username,
        email = email,
        userType = userType.toUserTypeDatabase(),
    )
}

fun TabEntryEntity.toTabEntry(): TabEntry {
    return TabEntry(
        id = id!!,
        groupId = groupId,
        creator = creator.toGroupParticipant(),
        paidBy = paidBy.toGroupParticipant(),
        title = title,
        description = description,
        amount = amount,
        currency = currency,
        splits = splits.map { it.toTabEntrySplit() },
        createdAt = createdAt,
        lastModifiedAt = lastModifiedAt,
        lastModifiedBy = lastModifiedBy.toGroupParticipant(),
        version = version,
        deletedAt = deletedAt,
        deletedBy = deletedBy?.toGroupParticipant(),
    )
}

fun TabEntrySplitEntity.toTabEntrySplit(): TabEntrySplit {
    return TabEntrySplit(
        id = id!!,
        participantId = participantId,
        participant = participant?.toGroupParticipant(),
        split = splitType.toSplit(value),
        resolvedAmount = resolvedAmount,
    )
}

fun SplitType.toSplit(value: BigDecimal): Split {
    return when (this) {
        SplitType.EQUAL -> Split.Equal
        SplitType.EXACT_AMOUNT -> Split.ExactAmount(value)
        SplitType.PERCENTAGE -> Split.Percentage(value)
        SplitType.SHARES -> Split.Shares(value)
    }
}

fun Split.toSplitType(): SplitType {
    return when (this) {
        Split.Equal -> SplitType.EQUAL
        is Split.ExactAmount -> SplitType.EXACT_AMOUNT
        is Split.Percentage -> SplitType.PERCENTAGE
        is Split.Shares -> SplitType.SHARES
    }
}

fun Split.toValue(): BigDecimal {
    return when (this) {
        Split.Equal -> BigDecimal.ZERO
        is Split.ExactAmount -> amount
        is Split.Percentage -> percentage
        is Split.Shares -> shares
    }
}

fun UserTypeDatabase.toUserType(): UserType {
    return when (this) {
        UserTypeDatabase.REGISTERED -> UserType.REGISTERED
        UserTypeDatabase.ANONYMOUS -> UserType.ANONYMOUS
    }
}

fun UserType.toUserTypeDatabase(): UserTypeDatabase {
    return when (this) {
        UserType.REGISTERED -> UserTypeDatabase.REGISTERED
        UserType.ANONYMOUS -> UserTypeDatabase.ANONYMOUS
    }
}

fun TabEntryHistoryEntity.toTabEntryHistory(
    changedByParticipant: GroupParticipant,
    creator: GroupParticipant,
    paidBy: GroupParticipant,
    splits: List<TabEntrySplit>,
    deletedBy: GroupParticipant? = null,
): TabEntryHistory {
    return TabEntryHistory(
        historyId = id!!,
        changeType = changeType.toChangeType(),
        changedAt = changedAt,
        changedBy = changedByParticipant,
        tabEntry =
            TabEntry(
                id = tabEntryId,
                groupId = groupId,
                creator = creator,
                paidBy = paidBy,
                title = title,
                description = description,
                amount = amount,
                currency = currency,
                splits = splits,
                createdAt = originalCreatedAt,
                lastModifiedAt = changedAt,
                lastModifiedBy = changedByParticipant,
                version = version,
                deletedAt = if (changeType == ChangeTypeDatabase.DELETED) changedAt else null,
                deletedBy = deletedBy,
            ),
    )
}

fun ChangeTypeDatabase.toChangeType(): ChangeType {
    return when (this) {
        ChangeTypeDatabase.CREATED -> ChangeType.CREATED
        ChangeTypeDatabase.UPDATED -> ChangeType.UPDATED
        ChangeTypeDatabase.DELETED -> ChangeType.DELETED
    }
}

fun ChangeType.toChangeTypeDatabase(): ChangeTypeDatabase {
    return when (this) {
        ChangeType.CREATED -> ChangeTypeDatabase.CREATED
        ChangeType.UPDATED -> ChangeTypeDatabase.UPDATED
        ChangeType.DELETED -> ChangeTypeDatabase.DELETED
    }
}
