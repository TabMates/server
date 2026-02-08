package de.tabmates.server.groups.api.mappers

import de.tabmates.server.groups.api.dto.GroupDto
import de.tabmates.server.groups.api.dto.GroupParticipantDto
import de.tabmates.server.groups.api.dto.SplitDto
import de.tabmates.server.groups.api.dto.TabEntryDto
import de.tabmates.server.groups.api.dto.TabEntrySplitDto
import de.tabmates.server.groups.api.dto.UserTypeDto
import de.tabmates.server.groups.domain.model.Group
import de.tabmates.server.groups.domain.model.GroupParticipant
import de.tabmates.server.groups.domain.model.Split
import de.tabmates.server.groups.domain.model.TabEntry
import de.tabmates.server.groups.domain.model.TabEntrySplit
import de.tabmates.server.groups.domain.model.UserType

fun TabEntry.toTabEntryDto(): TabEntryDto {
    return TabEntryDto(
        id = id,
        groupId = groupId,
        creator = creator.toGroupParticipantDto(),
        paidBy = paidBy.toGroupParticipantDto(),
        title = title,
        description = description,
        amount = amount,
        currency = currency,
        splits = splits.map { it.toTabEntrySplitDto() },
        createdAt = createdAt,
        lastModifiedAt = lastModifiedAt,
        lastModifiedBy = lastModifiedBy.toGroupParticipantDto(),
        version = version,
        deletedAt = deletedAt,
        deletedBy = deletedBy?.toGroupParticipantDto(),
    )
}

fun Group.toGroupDto(): GroupDto {
    return GroupDto(
        id = id,
        participants = participants.map { it.toGroupParticipantDto() }.toSet(),
        creator = creator.toGroupParticipantDto(),
        lastActivityAt = lastActivityAt,
        createdAt = createdAt,
    )
}

fun GroupParticipant.toGroupParticipantDto(): GroupParticipantDto {
    return GroupParticipantDto(
        userId = userId,
        username = username,
        email = email,
        userType = userType.toUserTypeDto(),
    )
}

fun UserType.toUserTypeDto(): UserTypeDto {
    return when (this) {
        UserType.REGISTERED -> UserTypeDto.REGISTERED
        UserType.ANONYMOUS -> UserTypeDto.ANONYMOUS
    }
}

fun TabEntrySplit.toTabEntrySplitDto(): TabEntrySplitDto {
    return TabEntrySplitDto(
        id = id,
        participantId = participantId,
        participant = participant?.toGroupParticipantDto(),
        split = split.toSplitDto(),
        resolvedAmount = resolvedAmount,
    )
}

fun Split.toSplitDto(): SplitDto {
    return when (this) {
        is Split.Equal -> SplitDto.Equal
        is Split.ExactAmount -> SplitDto.ExactAmount(amount)
        is Split.Percentage -> SplitDto.Percentage(percentage)
        is Split.Shares -> SplitDto.Shares(shares)
    }
}
