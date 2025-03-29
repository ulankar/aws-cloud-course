package modules.home_tasks.aws_sdk.tests.module_6.utils;

import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SecureRDSDataReader {
    private final RDSSecureConnector connector;

    public SecureRDSDataReader(RDSSecureConnector connector) {
        this.connector = connector;
    }

    public List<ImageMetadata> getImageMetadata() throws Exception {
        List<ImageMetadata> images = new ArrayList<>();
        String query = "SELECT object_key, object_size, object_type, last_modified FROM images";

        try (Connection connection = connector.connectToRDS();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                ImageMetadata image = new ImageMetadata(
                        rs.getString("object_key"),
                        rs.getLong("object_size"),
                        rs.getString("object_type"),
                        rs.getString("last_modified")
                );
                images.add(image);
            }
        } finally {
            connector.closeSSHTunnel();
        }

        return images;
    }

    @Getter
    public static class ImageMetadata {
        private final String imageKey;
        private final long size;
        private final String imageType;
        private final String lastModified;

        public ImageMetadata(String imageKey, long size, String imageType, String lastModified) {
            this.imageKey = imageKey;
            this.size = size;
            this.imageType = imageType;
            this.lastModified = lastModified;
        }
    }
}
