package de.tabmates.server.groups.api.controllers

import de.tabmates.server.common.api.util.requestedUserId
import de.tabmates.server.common.domain.type.TabEntryId
import de.tabmates.server.groups.service.TabEntryService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/tab-entry")
class TabEntryController(
    private val tabEntryService: TabEntryService,
) {
    @DeleteMapping("{tabEntryId}")
    fun deleteTabEntry(
        @PathVariable("tabEntryId") tabEntryId: TabEntryId,
    ) {
        tabEntryService.deleteTabEntry(
            tabEntryId = tabEntryId,
            deletedByUserId = requestedUserId,
        )
    }
}
