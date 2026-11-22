package com.clrs.mom.producer;

import jakarta.annotation.PreDestroy;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class LineProducerService {

    private final RabbitTemplate rabbitTemplate;
    private final ExecutorService executorService;

    public LineProducerService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public void startReading(Path filePath, List<String> keywords) {
        LineReaderTask evenReader = new LineReaderTask(filePath, keywords, true, rabbitTemplate);
        LineReaderTask oddReader = new LineReaderTask(filePath, keywords, false, rabbitTemplate);

        executorService.submit(evenReader);
        executorService.submit(oddReader);
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }
}