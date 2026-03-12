package com.orangehrm.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties = new Properties();

    static {
        try (InputStream input = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("No se encontró config.properties, usando valores por defecto");
                setDefaultProperties();
            }
        } catch (IOException e) {
            e.printStackTrace();
            setDefaultProperties();
        }
    }

    private static void setDefaultProperties() {
        properties.setProperty("app.url", "https://www.interrapidisimo.com");
        properties.setProperty("app.timeout", "15");
        properties.setProperty("browser", "chrome");
        properties.setProperty("headless", "false");
        properties.setProperty("screenshot.path", "screenshots/");
        properties.setProperty("report.path", "test-output/");
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static boolean getBooleanProperty(String key, boolean b) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    public static int getIntProperty(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }
}