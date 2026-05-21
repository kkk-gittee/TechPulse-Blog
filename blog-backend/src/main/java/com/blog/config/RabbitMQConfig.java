package com.blog.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQConfig {
    public static final String EXCHANGE_NOTIFICATION = "notification.exchange";
    public static final String QUEUE_LIKE = "notification.like";
    public static final String QUEUE_COMMENT = "notification.comment";
    public static final String QUEUE_FOLLOW = "notification.follow";
    public static final String ROUTING_KEY_LIKE = "notification.like";
    public static final String ROUTING_KEY_COMMENT = "notification.comment";
    public static final String ROUTING_KEY_FOLLOW = "notification.follow";

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(EXCHANGE_NOTIFICATION);
    }

    @Bean
    public Queue likeQueue() {
        return QueueBuilder.durable(QUEUE_LIKE).build();
    }

    @Bean
    public Queue commentQueue() {
        return QueueBuilder.durable(QUEUE_COMMENT).build();
    }

    @Bean
    public Queue followQueue() {
        return QueueBuilder.durable(QUEUE_FOLLOW).build();
    }

    @Bean
    public Binding likeBinding(Queue likeQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(likeQueue).to(notificationExchange).with(ROUTING_KEY_LIKE);
    }

    @Bean
    public Binding commentBinding(Queue commentQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(commentQueue).to(notificationExchange).with(ROUTING_KEY_COMMENT);
    }

    @Bean
    public Binding followBinding(Queue followQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(followQueue).to(notificationExchange).with(ROUTING_KEY_FOLLOW);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
