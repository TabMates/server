package de.tabmates.server.groups.infra.messaging

import de.tabmates.server.common.domain.events.user.UserEvent
import de.tabmates.server.common.infra.messagequeue.MessageQueues
import de.tabmates.server.groups.domain.model.GroupParticipant
import de.tabmates.server.groups.domain.model.UserType
import de.tabmates.server.groups.service.GroupParticipantService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class TabGroupUserEventListener(
    private val groupParticipantService: GroupParticipantService,
) {
    @RabbitListener(queues = [MessageQueues.TABGROUP_USER_EVENTS])
    fun handleUserEvent(event: UserEvent) {
        when (event) {
            is UserEvent.Verified -> {
                groupParticipantService.createGroupParticipant(
                    groupParticipant =
                        GroupParticipant(
                            userId = event.userId,
                            username = event.username,
                            email = event.email,
                            userType = UserType.REGISTERED,
                        ),
                )
            }

            is UserEvent.AnonymousUserCreated -> {
                groupParticipantService.createGroupParticipant(
                    groupParticipant =
                        GroupParticipant(
                            userId = event.userId,
                            username = event.username,
                            email = null,
                            userType = UserType.ANONYMOUS,
                        ),
                )
            }

            else -> {
                Unit
            }
        }
    }
}
