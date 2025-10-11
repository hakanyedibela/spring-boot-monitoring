
package hkn7b.dev.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

@Component
public class JsonLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonLogger.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public void info(String message) {
        log("INFO", message, null, null);
    }
    
    public void info(String message, Map<String, Object> fields) {
        log("INFO", message, fields, null);
    }
    
    public void warn(String message) {
        log("WARN", message, null, null);
    }
    
    public void warn(String message, Map<String, Object> fields) {
        log("WARN", message, fields, null);
    }
    
    public void error(String message) {
        log("ERROR", message, null, null);
    }
    
    public void error(String message, Throwable throwable) {
        log("ERROR", message, null, throwable);
    }
    
    public void error(String message, Map<String, Object> fields, Throwable throwable) {
        log("ERROR", message, fields, throwable);
    }
    
    public void debug(String message) {
        log("DEBUG", message, null, null);
    }
    
    public void debug(String message, Map<String, Object> fields) {
        log("DEBUG", message, fields, null);
    }
    
    private void log(String level, String message, Map<String, Object> fields, Throwable throwable) {
        try {
            ObjectNode logEntry = objectMapper.createObjectNode();
            logEntry.put("@timestamp", Instant.now().toString());
            logEntry.put("level", level);
            logEntry.put("message", message);
            logEntry.put("logger", getCallerClassName());
            logEntry.put("thread", Thread.currentThread().getName());
            
            // Add MDC context
            Map<String, String> mdcContext = MDC.getCopyOfContextMap();
            if (mdcContext != null && !mdcContext.isEmpty()) {
                ObjectNode contextNode = objectMapper.createObjectNode();
                mdcContext.forEach(contextNode::put);
                logEntry.set("mdc", contextNode);
            }
            
            // Add custom fields
            if (fields != null && !fields.isEmpty()) {
                ObjectNode fieldsNode = objectMapper.valueToTree(fields);
                logEntry.set("fields", fieldsNode);
            }
            
            // Add exception details if present
            if (throwable != null) {
                ObjectNode exceptionNode = objectMapper.createObjectNode();
                exceptionNode.put("class", throwable.getClass().getName());
                exceptionNode.put("message", throwable.getMessage());
                
                // Add stack trace
                StringBuilder stackTrace = new StringBuilder();
                for (StackTraceElement element : throwable.getStackTrace()) {
                    stackTrace.append(element.toString()).append("\n");
                }
                exceptionNode.put("stackTrace", stackTrace.toString());
                
                logEntry.set("exception", exceptionNode);
            }
            
            String jsonLog = objectMapper.writeValueAsString(logEntry);
            
            switch (level) {
                case "ERROR":
                    logger.error(jsonLog);
                    break;
                case "WARN":
                    logger.warn(jsonLog);
                    break;
                case "DEBUG":
                    logger.debug(jsonLog);
                    break;
                case "TRACE":
                    logger.trace(jsonLog);
                    break;
                default:
                    logger.info(jsonLog);
            }
        } catch (Exception e) {
            logger.error("Failed to create JSON log entry: " + message, e);
        }
    }
    
    private String getCallerClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // Skip getStackTrace, getCallerClassName, log, and the public method
        if (stackTrace.length > 4) {
            return stackTrace[4].getClassName();
        }
        return "Unknown";
    }
    
    public static class LogBuilder {
        private final JsonLogger jsonLogger;
        private final String level;
        private final String message;
        private final Map<String, Object> fields = new HashMap<>();
        private Throwable throwable;
        
        public LogBuilder(JsonLogger jsonLogger, String level, String message) {
            this.jsonLogger = jsonLogger;
            this.level = level;
            this.message = message;
        }
        
        public LogBuilder field(String key, Object value) {
            fields.put(key, value);
            return this;
        }
        
        public LogBuilder fields(Map<String, Object> fields) {
            this.fields.putAll(fields);
            return this;
        }
        
        public LogBuilder exception(Throwable throwable) {
            this.throwable = throwable;
            return this;
        }
        
        public void log() {
            jsonLogger.log(level, message, fields.isEmpty() ? null : fields, throwable);
        }
    }
    
    public LogBuilder builder(String level, String message) {
        return new LogBuilder(this, level, message);
    }
}
