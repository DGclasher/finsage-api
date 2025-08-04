package org.finsage.api;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ApiApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		System.setProperty("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID"));
		System.setProperty("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET"));
		System.setProperty("GOOGLE_REDIRECT_URI", dotenv.get("GOOGLE_REDIRECT_URI"));
		System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));

		System.setProperty("POSTGRES_DB", dotenv.get("POSTGRES_DB"));
		System.setProperty("POSTGRES_USER", dotenv.get("POSTGRES_USER"));
		System.setProperty("POSTGRES_PASSWORD", dotenv.get("POSTGRES_PASSWORD"));
		System.setProperty("POSTGRES_HOST", dotenv.get("POSTGRES_HOST"));
		System.setProperty("POSTGRES_PORT", dotenv.get("POSTGRES_PORT"));
		System.setProperty("REDIS_HOST", dotenv.get("REDIS_HOST"));
		System.setProperty("REDIS_PORT", dotenv.get("REDIS_PORT"));

		SpringApplication.run(ApiApplication.class, args);
	}

}
