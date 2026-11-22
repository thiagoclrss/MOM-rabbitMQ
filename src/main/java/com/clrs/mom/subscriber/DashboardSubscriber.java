package com.clrs.mom.subscriber;

import com.clrs.mom.config.RabbitMQConfig;
import com.clrs.mom.dto.WordCountMessage;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class DashboardSubscriber {

    private Consumer<WordCountMessage> uiCallback;

    public void setOnMessageReceived(Consumer<WordCountMessage> callback) {
        this.uiCallback = callback;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "", durable = "false", exclusive = "true", autoDelete = "true"),
            exchange = @Exchange(value = RabbitMQConfig.TOPIC_WORDS, type = ExchangeTypes.TOPIC),
            key = "words.#"
    ))
    public void onWordCountReceived(WordCountMessage message) {
        if (uiCallback != null) {
            uiCallback.accept(message);
        }
    }
}