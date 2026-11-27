package com.example.aiservice;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AiServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiServiceApplication.class, args);
	}

	/** Relogio injetado nos casos de uso: os testes fixam a hora de autorizadoEm. */
	@Bean
	Clock relogio() {
		return Clock.systemUTC();
	}

}
