package org.finsage.api;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ApiApplication {

	private static final Logger logger = LoggerFactory.getLogger(ApiApplication.class);

	public static void main(String[] args) {
		try {
			loadEnvironmentVariables();
			SpringApplication.run(ApiApplication.class, args);
		} catch (Exception e) {
			logger.error("Failed to start application: {}", e.getMessage(), e);
			System.exit(1);
		}
	}

	private static void loadEnvironmentVariables() {
		try {
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMissing()
					.load();

			// Required environment variables
			String[] requiredVars = {
					"GOOGLE_CLIENT_ID", "GOOGLE_CLIENT_SECRET", "GOOGLE_REDIRECT_URI",
					"JWT_SECRET", "POSTGRES_DB", "POSTGRES_USER", "POSTGRES_PASSWORD",
					"POSTGRES_HOST", "POSTGRES_PORT", "REDIS_HOST", "REDIS_PORT"
			};

			for (String var : requiredVars) {
				String value = dotenv.get(var);
				if (value == null || value.trim().isEmpty()) {
					logger.warn("Environment variable {} is not set or empty, using default if available", var);
				}
				System.setProperty(var, value != null ? value : "");
			}

			logger.info("Environment variables loaded successfully");

		} catch (DotenvException e) {
			logger.warn("Could not load .env file: {}. Using system environment variables.", e.getMessage());
		} catch (Exception e) {
			logger.error("Error loading environment variables: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to load environment variables", e);
		}
	}

}
