package de.tabmates.server.common.infra.messagequeue

import de.tabmates.server.common.domain.events.TabMatesEvent
import de.tabmates.server.common.domain.events.user.UserEventConstants
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import tools.jackson.databind.DefaultTyping
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import tools.jackson.module.kotlin.kotlinModule

@Configuration
@EnableTransactionManagement
class RabbitMqConfig {
    @Bean
    fun messageConverter(): JacksonJsonMessageConverter {
        val polymorphicTypeValidator =
            BasicPolymorphicTypeValidator
                .builder()
                .allowIfBaseType(TabMatesEvent::class.java)
                .allowIfSubType("java.util.") // Allow Java lists
                .allowIfSubType("kotlin.collections.") // Kotlin collections
                .build()

        val objectMapper =
            JsonMapper
                .builder()
                .addModule(kotlinModule())
                .polymorphicTypeValidator(polymorphicTypeValidator)
                .activateDefaultTyping(polymorphicTypeValidator, DefaultTyping.NON_FINAL)
                .build()

        return JacksonJsonMessageConverter(objectMapper).apply {
            typePrecedence = JacksonJavaTypeMapper.TypePrecedence.TYPE_ID
        }
    }

    @Bean
    fun rabbitListenerContainerFactory(
        connectionFactory: ConnectionFactory,
        transactionManager: PlatformTransactionManager,
        messageConverter: JacksonJsonMessageConverter,
    ): SimpleRabbitListenerContainerFactory {
        return SimpleRabbitListenerContainerFactory().apply {
            this.setConnectionFactory(connectionFactory)
            this.setTransactionManager(transactionManager)
            this.setChannelTransacted(true)
            this.setMessageConverter(messageConverter)
        }
    }

    @Bean
    fun rabbitTemplate(
        connectionFactory: ConnectionFactory,
        messageConverter: JacksonJsonMessageConverter,
    ): RabbitTemplate {
        return RabbitTemplate(connectionFactory).apply {
            this.messageConverter = messageConverter
        }
    }

    @Bean
    fun userExchange() =
        TopicExchange(
            UserEventConstants.USER_EXCHANGE,
            true,
            false,
        )

    @Bean
    fun tabGroupUserEventsQueue() =
        Queue(
            MessageQueues.TABGROUP_USER_EVENTS,
            true,
        )

    @Bean
    fun notificationUserEventsQueue() =
        Queue(
            MessageQueues.NOTIFICATION_USER_EVENTS,
            true,
        )

    @Bean
    fun tabGroupUserEventsBinding(
        tabGroupUserEventsQueue: Queue,
        userExchange: TopicExchange,
    ): Binding {
        return BindingBuilder
            .bind(tabGroupUserEventsQueue)
            .to(userExchange)
            .with("user.*")
    }

    @Bean
    fun notificationUserEventsBinding(
        notificationUserEventsQueue: Queue,
        userExchange: TopicExchange,
    ): Binding {
        return BindingBuilder
            .bind(notificationUserEventsQueue)
            .to(userExchange)
            .with("user.*")
    }
}
