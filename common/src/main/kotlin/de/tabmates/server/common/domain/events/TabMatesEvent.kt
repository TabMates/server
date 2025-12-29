package de.tabmates.server.common.domain.events

import java.time.Instant

interface TabMatesEvent {
    val eventId: String
    val eventKey: String
    val occurredAt: Instant
    val exchange: String
}
