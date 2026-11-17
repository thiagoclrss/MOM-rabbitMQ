package com.clrs.mom.config;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.getStage();

        StackPane root = new StackPane(new Label("Dashboard Inicializado com Spring!"));
        Scene scene = new Scene(root, 800, 600);

        stage.setScene(scene);
        stage.setTitle("Projeto MOM - Dashboard");
        stage.show();
    }
}
