package de.tabmates.server.groups.infra.database.mappers

import de.tabmates.server.groups.domain.model.Group
import de.tabmates.server.groups.domain.model.GroupParticipant
import de.tabmates.server.groups.domain.model.TabEntry
import de.tabmates.server.groups.domain.model.UserType
import de.tabmates.server.groups.infra.database.entries.GroupEntity
import de.tabmates.server.groups.infra.database.entries.GroupParticipantEntity
import de.tabmates.server.groups.infra.database.entries.TabEntryEntity
import de.tabmates.server.groups.infra.database.entries.UserTypeDatabase

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
        userType = userType.toUserType(),
    )
}

fun TabEntryEntity.toTabEntry(): TabEntry {
    return TabEntry(
        id = id!!,
        groupId = groupId,
        creator = creator.toGroupParticipant(),
        title = title,
        description = description,
        amount = amount,
        currency = currency,
        createdAt = createdAt,
    )
}

fun UserTypeDatabase.toUserType(): UserType {
    return when (this) {
        UserTypeDatabase.REGISTERED -> UserType.REGISTERED
        UserTypeDatabase.ANONYMOUS -> UserType.ANONYMOUS
    }
}

fun UserType.toUserType(): UserTypeDatabase {
    return when (this) {
        UserType.REGISTERED -> UserTypeDatabase.REGISTERED
        UserType.ANONYMOUS -> UserTypeDatabase.ANONYMOUS
    }
}
