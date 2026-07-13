package com.cvserbaada.smartinventory.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public final class DatabaseConfig {
    private static final String CONFIG_FILE = "application.properties";
    private static final String EXTERNAL_CONFIG_PROPERTY = "smartinventory.config";
    private static final Properties PROPERTIES = loadProperties();

    private DatabaseConfig() {
    }

    public static String getUrl() {
        return getRequiredProperty("db.url");
    }

    public static String getUsername() {
        return getRequiredProperty("db.username");
    }

    public static String getPassword() {
        String value = PROPERTIES.getProperty("db.password");
        return value == null ? "" : value.trim();
    }

    private static String getRequiredProperty(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required database property: " + key);
        }
        return value.trim();
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        loadClasspathProperties(properties);
        loadExternalProperties(properties);
        return properties;
    }

    private static void loadClasspathProperties(Properties properties) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(CONFIG_FILE)) {
            Objects.requireNonNull(inputStream, "Configuration file not found: " + CONFIG_FILE);
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load configuration file: " + CONFIG_FILE, exception);
        }
    }

    private static void loadExternalProperties(Properties properties) {
        for (Path path : getExternalConfigPaths()) {
            if (!Files.isRegularFile(path)) {
                continue;
            }

            try (InputStream inputStream = Files.newInputStream(path)) {
                properties.load(inputStream);
                return;
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to load external database configuration: " + path, exception);
            }
        }
    }

    private static List<Path> getExternalConfigPaths() {
        String customConfigPath = System.getProperty(EXTERNAL_CONFIG_PROPERTY);
        Path userDirectoryConfig = Path.of(System.getProperty("user.dir"), CONFIG_FILE);
        Path userHomeConfig = Path.of(System.getProperty("user.home"), "SmartInventorySystem", CONFIG_FILE);

        if (customConfigPath == null || customConfigPath.isBlank()) {
            return List.of(userDirectoryConfig, userHomeConfig);
        }

        return List.of(Path.of(customConfigPath.trim()), userDirectoryConfig, userHomeConfig);
    }
}
