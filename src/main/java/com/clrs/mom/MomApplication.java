package com.clrs.mom;

import com.clrs.mom.config.MomJavaFxApplication;
import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MomApplication {

	public static void main(String[] args) {
		Application.launch(MomJavaFxApplication.class, args);
	}

}
