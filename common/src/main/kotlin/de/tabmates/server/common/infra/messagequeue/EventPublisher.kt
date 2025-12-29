package de.tabmates.server.common.infra.messagequeue

import de.tabmates.server.common.domain.events.TabMatesEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class EventPublisher(
    private val rabbitTemplate: RabbitTemplate,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun <T : TabMatesEvent> publish(event: T) {
        try {
            rabbitTemplate.convertAndSend(
                event.exchange,
                event.eventKey,
                event,
            )
            logger.info("Published event: ${event.eventKey} to exchange: ${event.exchange}")
        } catch (e: Exception) {
            logger.error("Failed to publish event: ${event.eventKey} to exchange: ${event.exchange}", e)
        }
    }
}
