package Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central configuration. Resolution order: JVM system property, environment variable,
 * config.properties (non-secret defaults only). Credentials are never read from a file.
 */
public final class Config {
    private static final Properties FILE = load();

    private Config() {
    }

    public static String baseUrl() {
        return get("baseUrl", "ORANGEHRM_BASE_URL", "https://opensource-demo.orangehrmlive.com");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless", "ORANGEHRM_HEADLESS", "false"));
    }

    public static String adminUsername() {
        return require("admin.username", "ORANGEHRM_ADMIN_USER");
    }

    public static String adminPassword() {
        return require("admin.password", "ORANGEHRM_ADMIN_PASS");
    }

    private static String get(String propertyKey, String envKey, String defaultValue) {
        String value = System.getProperty(propertyKey);
        if (isBlank(value)) {
            value = System.getenv(envKey);
        }
        if (isBlank(value)) {
            value = FILE.getProperty(propertyKey);
        }
        return isBlank(value) ? defaultValue : value.trim();
    }

    private static String require(String propertyKey, String envKey) {
        String value = System.getProperty(propertyKey);
        if (isBlank(value)) {
            value = System.getenv(envKey);
        }
        if (isBlank(value)) {
            throw new IllegalStateException("Missing credential: set environment variable "
                    + envKey + " or JVM property -D" + propertyKey);
        }
        return value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read config.properties", e);
        }
        return properties;
    }
}
