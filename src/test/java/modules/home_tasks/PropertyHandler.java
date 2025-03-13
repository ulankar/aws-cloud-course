package modules.home_tasks;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertyHandler {

    private static final Properties PROPERTIES = new Properties();
    private static final String PROPERTY_FILENAME = getPropertyFilename();

    private PropertyHandler() {
        throw new IllegalStateException("Utility class is not meant to be instantiated");
    }

    public static void setPropertiesForEnvironment() {
        PROPERTIES.clear();

        try {
            PROPERTIES.load(getPropertiesAsStream());
        } catch (final IOException e) {
            throw new IllegalArgumentException("Cannot open property file " + PROPERTY_FILENAME, e);
        }
    }

    public static String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }

    private static InputStream getPropertiesAsStream() {
        return PropertyHandler.class.getClassLoader().getResourceAsStream(PROPERTY_FILENAME);
    }

    private static String getPropertyFilename() {
        return "properties/application.properties";
    }
}
