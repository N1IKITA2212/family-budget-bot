package com.example.familybudgetbot;

import com.example.familybudgetbot.config.TelegramConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(TelegramConfig.class)
@EnableScheduling
public class FamilybudgetbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(FamilybudgetbotApplication.class, args);
	}

}
