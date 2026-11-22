package com.clrs.mom.producer;

import com.clrs.mom.config.RabbitMQConfig;
import com.clrs.mom.dto.LineMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class LineReaderTask implements Runnable {

    private final Path filePath;
    private final List<String> keywords;
    private final boolean readEvenLines;
    private final RabbitTemplate rabbitTemplate;

    public LineReaderTask(Path filePath,
                          List<String> keywords,
                          boolean readEvenLines,
                          RabbitTemplate rabbitTemplate) {
        this.filePath = filePath;
        this.keywords = keywords;
        this.readEvenLines = readEvenLines;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void run() {
        String readerType = readEvenLines ? "PARES" : "ÍMPARES";
        log.info("Iniciando leitor de linhas {} para o arquivo: {}", readerType, filePath.getFileName());

        int linesSent = 0;

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                boolean isEven = (lineNumber % 2 == 0);

                if (isEven == readEvenLines && !line.isBlank()) {
                    LineMessage message = new LineMessage(lineNumber, line, keywords);
                    rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_LINES, message);
                    linesSent++;
                }
                lineNumber++;
            }

            log.info("Leitor de linhas {} finalizado. Total enviadas: {}", readerType, linesSent);
        } catch (IOException e) {
            log.error("Erro ao ler o arquivo no leitor de linhas {}: {}", readerType, e.getMessage(), e);
        }
    }
}
