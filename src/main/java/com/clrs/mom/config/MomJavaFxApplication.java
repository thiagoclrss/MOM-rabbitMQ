package com.clrs.mom.config;

import com.clrs.mom.MomApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class MomJavaFxApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Inicializa o Spring Boot em background sem abrir um servidor web
        springContext = new SpringApplicationBuilder(MomApplication.class)
                .headless(false) // Necessário para permitir a UI do JavaFX
                .run();
    }

    @Override
    public void start(Stage primaryStage) {
        // Aqui você carregará o seu arquivo FXML ou criará a UI programaticamente
        // O springContext pode ser usado para buscar seus Beans (controllers, serviços)
        springContext.publishEvent(new StageReadyEvent(primaryStage));
    }

    @Override
    public void stop() {
        springContext.close();
        Platform.exit();
    }
}