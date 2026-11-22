package com.clrs.mom.worker;

import com.clrs.mom.config.RabbitMQConfig;
import com.clrs.mom.dto.LineMessage;
import com.clrs.mom.dto.WordCountMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class WordCountWorker {

    public static final String ROUTING_KEY = "words.count";

    private final RabbitTemplate rabbitTemplate;

    public WordCountWorker(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_LINES, concurrency = "3-5")
    public void processLine(LineMessage message) {
        Map<String, Integer> counts = new HashMap<>();
        String content = message.content();

        for (String keyword : message.keywords()) {
            if (keyword == null || keyword.isBlank()) {
                continue;
            }

            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(keyword.trim()) + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);
            int count = 0;
            while (matcher.find()) {
                count++;
            }
            log.debug("Encontradas {} ocorrências da palavra-chave '{}' na linha {}", count, keyword.trim(), message.lineNumber());

            if (count > 0) {
                counts.put(keyword.trim(), count);
            }
        }

        if (!counts.isEmpty()) {
            WordCountMessage result = new WordCountMessage(message.lineNumber(), counts);
            rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_WORDS, ROUTING_KEY, result);
            log.debug("[{}] Linha {} processada. Encontradas: {}", Thread.currentThread().getName(), message.lineNumber(), counts);
        }
    }
}