package teneo.MenuExplorer.server;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * This is the main server entrypoint. It loads @MenuSmartSearch's model file,
 * then starts the spring server
 */
@SpringBootApplication
@ComponentScan(basePackages = "teneo.MenuExplorer.server")
public class MenuExplorerServer {

	public static void main(String[] args) {
		System.out.println("Menu Explorer Server started!");

		System.out.println("Loading model...");
		Instant start = Instant.now();

		String modelPath = null;

		for (String arg : args) {
			if (arg.startsWith("--modelPath=")) {
				modelPath = arg.substring("--modelPath=".length());
				break;
			}
		}

		try {
			if (modelPath == null) {
				Properties props = new Properties();
				props.load(MenuExplorerServer.class.getClassLoader().getResourceAsStream("application.properties"));
				modelPath = props.getProperty("modelPath");
			}

			if (modelPath != null) {
				MenuSmartSearch.loadModel(modelPath);
			} else {
				System.out.println("Model path not specified, continuing without loading model...");
			}

		} catch (IOException e) {
			System.out.println("Error loading model file: " + e.getMessage());
			System.out.println("Continuing without loading model...");
		}
		Instant end = Instant.now();
		System.out.println("Finished loading model in: " + Duration.between(start, end).getSeconds() + " seconds");
		System.out.println("Starting spring server...");
		SpringApplication.run(MenuExplorerServer.class, args);
	}
}
