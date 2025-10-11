package hkn7b.dev.controller;

import hkn7b.dev.logger.JsonLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MonitoringController {

    @Autowired
    private JsonLogger jsonLogger;

    @GetMapping("/example")
    public String example() {
        // Simple logging
        jsonLogger.info("Processing example request");

        // Logging with fields
        jsonLogger.info("User action performed", Map.of(
                "userId", "12345",
                "action", "view_page",
                "page", "/example"
        ));

        // Using builder pattern
        jsonLogger.builder("INFO", "Complex operation completed")
                .field("duration", 150)
                .field("status", "success")
                .field("recordsProcessed", 42)
                .log();

        return "Example completed";
    }
}
