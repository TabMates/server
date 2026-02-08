package de.tabmates.server.groups.api.controllers

import de.tabmates.server.common.api.util.requestedUserId
import de.tabmates.server.common.domain.type.GroupId
import de.tabmates.server.groups.api.dto.AddParticipantToGroupDto
import de.tabmates.server.groups.api.dto.CreateGroupRequestDto
import de.tabmates.server.groups.api.dto.GroupDto
import de.tabmates.server.groups.api.dto.TabEntryDto
import de.tabmates.server.groups.api.mappers.toGroupDto
import de.tabmates.server.groups.service.GroupService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@RestController
@RequestMapping("/api/group")
class GroupController(
    private val groupService: GroupService,
) {
    @GetMapping("{groupId}/tab-entries")
    fun getTabEntriesForGroup(
        @PathVariable("groupId") groupId: GroupId,
        @RequestParam("before", required = false) before: Instant? = null,
        @RequestParam("pageSize", required = false) pageSize: Int = DEFAULT_TAB_ENTRIES_PAGE_SIZE,
    ): List<TabEntryDto> {
        return groupService.getTabEntries(
            groupId = groupId,
            before = before ?: Instant.now(),
            pageSize = pageSize,
        )
    }

    @GetMapping("{groupId}")
    fun getGroup(
        @PathVariable("groupId") groupId: GroupId,
    ): GroupDto {
        return groupService
            .getGroupById(
                groupId = groupId,
                requestedUserId = requestedUserId,
            )?.toGroupDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @GetMapping
    fun getGroupsForUser(): List<GroupDto> {
        return groupService
            .findGroupsByUser(
                userId = requestedUserId,
            ).map { it.toGroupDto() }
    }

    @PostMapping
    fun createGroup(
        @Valid @RequestBody body: CreateGroupRequestDto,
    ): GroupDto {
        return groupService
            .createGroup(
                creatorId = requestedUserId,
                participantsUserIds = body.otherParticipantUserIds.toSet(),
            ).toGroupDto()
    }

    @PostMapping("/{groupId}/add")
    fun addGroupParticipant(
        @PathVariable("groupId") groupId: GroupId,
        @Valid @RequestBody body: AddParticipantToGroupDto,
    ): GroupDto {
        return groupService
            .addParticipantsToGroup(
                requestedUserId = requestedUserId,
                groupId = groupId,
                userIds = body.userIds.toSet(),
            ).toGroupDto()
    }

    @DeleteMapping("/{groupId}/leave")
    fun leaveGroup(
        @PathVariable("groupId") groupId: GroupId,
    ) {
        return groupService.removeParticipantFromGroup(
            groupId = groupId,
            userId = requestedUserId,
        )
    }

    companion object {
        private const val DEFAULT_TAB_ENTRIES_PAGE_SIZE = 20
    }
}
