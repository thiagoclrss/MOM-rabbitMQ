package com.clrs.mom.config; // Ajuste para o pacote onde está o seu StageInitializer

import com.clrs.mom.dto.WordCountMessage;
import com.clrs.mom.producer.LineProducerService;
import com.clrs.mom.subscriber.DashboardSubscriber;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    private final LineProducerService lineProducerService;
    private final DashboardSubscriber dashboardSubscriber;

    private Path selectedFilePath;
    private final ObservableList<WordRow> tableData = FXCollections.observableArrayList();
    private final Map<String, WordRow> wordRowMap = new ConcurrentHashMap<>();

    public StageInitializer(LineProducerService lineProducerService, DashboardSubscriber dashboardSubscriber) {
        this.lineProducerService = lineProducerService;
        this.dashboardSubscriber = dashboardSubscriber;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();

        Label titleLabel = new Label("Contador de Palavras-Chave (MOM + RabbitMQ)");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField fileField = new TextField();
        fileField.setPromptText("Nenhum arquivo selecionado...");
        fileField.setEditable(false);
        HBox.setHgrow(fileField, Priority.ALWAYS);

        Button selectFileBtn = new Button("Selecionar Arquivo");
        selectFileBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecione o arquivo de texto");
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                selectedFilePath = file.toPath();
                fileField.setText(file.getAbsolutePath());
            }
        });

        HBox fileBox = new HBox(10, fileField, selectFileBtn);
        fileBox.setAlignment(Pos.CENTER_LEFT);

        TextField keywordsField = new TextField();
        keywordsField.setPromptText("Palavras-chave separadas por vírgula (ex: java, thread, dados)");
        HBox.setHgrow(keywordsField, Priority.ALWAYS);

        Button startBtn = new Button("Iniciar Contagem");
        startBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox keywordsBox = new HBox(10, keywordsField, startBtn);
        keywordsBox.setAlignment(Pos.CENTER_LEFT);

        Label statusLabel = new Label("Aguardando início...");

        // --- SEÇÃO DO DASHBOARD ---
        TableView<WordRow> tableView = new TableView<>(tableData);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<WordRow, String> wordCol = new TableColumn<>("Palavra-Chave");
        wordCol.setCellValueFactory(data -> data.getValue().wordProperty());

        TableColumn<WordRow, Number> countCol = new TableColumn<>("Ocorrências");
        countCol.setCellValueFactory(data -> data.getValue().countProperty());

        tableView.getColumns().addAll(wordCol, countCol);

        // --- AÇÃO DO BOTÃO INICIAR ---
        startBtn.setOnAction(e -> {
            if (selectedFilePath == null) {
                statusLabel.setText("Erro: Selecione um arquivo primeiro.");
                return;
            }
            String rawKeywords = keywordsField.getText();
            if (rawKeywords == null || rawKeywords.isBlank()) {
                statusLabel.setText("Erro: Informe pelo menos uma palavra-chave.");
                return;
            }

            List<String> keywords = Arrays.stream(rawKeywords.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .distinct()
                    .toList();

            // Reseta o Dashboard e inicializa as palavras com 0
            tableData.clear();
            wordRowMap.clear();
            for (String kw : keywords) {
                WordRow row = new WordRow(kw, 0);
                wordRowMap.put(kw.toLowerCase(), row);
                tableData.add(row);
            }

            statusLabel.setText("Processando arquivo em paralelo...");
            lineProducerService.startReading(selectedFilePath, keywords);
        });

        // --- CONEXÃO DO SUBSCRIBER COM A UI ---
        dashboardSubscriber.setOnMessageReceived(message -> updateDashboard(message, statusLabel));

        // --- LAYOUT PRINCIPAL ---
        VBox root = new VBox(15, titleLabel, fileBox, keywordsBox, statusLabel, tableView);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 750, 500);
        stage.setScene(scene);
        stage.setTitle("Projeto MOM - Cliente & Dashboard");
        stage.show();
    }

    private void updateDashboard(WordCountMessage message, Label statusLabel) {
        // Obrigatório no JavaFX: atualizações de UI vindas de threads externas (RabbitMQ)
        // devem ser agendadas na JavaFX Application Thread via Platform.runLater
        Platform.runLater(() -> {
            message.counts().forEach((word, count) -> {
                WordRow row = wordRowMap.get(word.toLowerCase());
                if (row != null) {
                    row.addCount(count);
                }
            });
            statusLabel.setText("Última atualização: linha " + message.lineNumber() + " processada.");
        });
    }

    // Classe auxiliar para exibir e atualizar as linhas da tabela reativamente
    public static class WordRow {
        private final StringProperty word;
        private final IntegerProperty count;

        public WordRow(String word, int initialCount) {
            this.word = new SimpleStringProperty(word);
            this.count = new SimpleIntegerProperty(initialCount);
        }

        public StringProperty wordProperty() {
            return word;
        }

        public IntegerProperty countProperty() {
            return count;
        }

        public void addCount(int delta) {
            this.count.set(this.count.get() + delta);
        }
    }
}