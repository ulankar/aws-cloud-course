package modules.home_tasks.aws_sdk.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileUtils {

    public static final String KEY_DIRECTORY = "src/test/resources/keys";
    public static final String KEY_FILE_NAME = "private-key.pem";

    public static void main(String[] args) {
        String privateKey = "-----BEGIN PRIVATE KEY-----" +
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCs.." +
                "-----END PRIVATE KEY-----";
        writePemFile(privateKey);
        System.out.println("Path to the file: " + getPathToPemFile());
    }

    public static void writePemFile(String privateKey) {
        try {
            Path dirPath = Paths.get(KEY_DIRECTORY);

            // Ensure directory exists
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // Create full path to the file
            Path filePath = dirPath.resolve(KEY_FILE_NAME);

            // Ensure LF (\n) line separator
            String formattedKey = privateKey.replace("\r\n", "\n").replace("\r", "\n");

            // Write file with UTF-8 encoding
            Files.write(filePath, formattedKey.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Set file to read-only for security
//            filePath.toFile().setReadable(true, true);
//            filePath.toFile().setWritable(false, true);

            System.out.println("Private key successfully written to " + filePath.toAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPathToPemFile() {
        return Paths.get(KEY_DIRECTORY).resolve(KEY_FILE_NAME).toString();
    }

    public static String readFile(String path) {
        try {
            Path filePath = Paths.get(path);
            String content = new String(Files.readAllBytes(filePath));
            System.out.println(content);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
