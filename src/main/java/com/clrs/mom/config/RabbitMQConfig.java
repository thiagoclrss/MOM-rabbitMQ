package com.clrs.mom.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_LINES = "queue_lines";
    public static final String TOPIC_WORDS = "topic_words";

    @Bean
    public Queue queueLines() {
        return new Queue(QUEUE_LINES, true);
    }

    @Bean
    public TopicExchange exchangeWords() {
        return new TopicExchange(TOPIC_WORDS);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
