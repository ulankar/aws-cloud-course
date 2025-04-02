package modules.home_tasks.aws_sdk.tests.module_6.utils;

import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

import static modules.home_tasks.aws_sdk.utils.AwsHooks.secretsClient;

public class SecretsManagerReader {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Retrieve a secret value as a string
     */
    public static String getSecretString(String secretName) {
        try {
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse response = secretsClient.getSecretValue(request);
            return response.secretString();

        } catch (SecretsManagerException e) {
            System.err.println("Error retrieving secret: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieve a JSON secret and parse it into a Map
     */
    public static Map<String, String> getJsonSecret(String secretName) {
        try {
            String secretString = getSecretString(secretName);
            return objectMapper.readValue(secretString,
                    new TypeReference<Map<String, String>>() {
                    });

        } catch (Exception e) {
            System.err.println("Error parsing JSON secret: " + e.getMessage());
            throw new RuntimeException("Failed to parse secret JSON", e);
        }
    }

    /**
     * Retrieve database credentials from a secret
     */
    public static DatabaseCredentials getDatabaseCredentials(String secretName) {
        try {
            Map<String, String> secretMap = getJsonSecret(secretName);

            return new DatabaseCredentials(
                    secretMap.get("username"),
                    secretMap.get("password"),
                    secretMap.get("host"),
                    secretMap.get("port"),
                    secretMap.get("dbname")
            );

        } catch (Exception e) {
            System.err.println("Error retrieving database credentials: " + e.getMessage());
            throw new RuntimeException("Failed to get database credentials", e);
        }
    }

    public static String getJdbcUrl(String host, String port, String name) {
        return String.format("jdbc:mysql://%s:%s/%s", host, port, name);
    }

    public static class DatabaseCredentials {
        private final String username;
        private final String password;
        private final String host;
        private final String port;
        private final String dbName;

        public DatabaseCredentials(String username, String password, String host,
                                   String port, String dbName) {
            this.username = username;
            this.password = password;
            this.host = host;
            this.port = port;
            this.dbName = dbName;
        }

        public String getJdbcUrl() {
            return String.format("jdbc:mysql://%s:%s/%s", host, port, dbName);
        }

        // Getters
        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getHost() {
            return host;
        }

        public String getPort() {
            return port;
        }

        public String getDbName() {
            return dbName;
        }
    }

}
