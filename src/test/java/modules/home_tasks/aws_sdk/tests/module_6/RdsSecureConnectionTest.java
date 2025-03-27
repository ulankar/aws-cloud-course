package modules.home_tasks.aws_sdk.tests.module_6;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import modules.home_tasks.aws_sdk.tests.module_5.SwaggerSteps;
import modules.home_tasks.aws_sdk.tests.module_6.utils.RDSSecureConnector;
import modules.home_tasks.aws_sdk.tests.module_6.utils.RdsHooks;
import modules.home_tasks.aws_sdk.tests.module_6.utils.SecureRDSDataReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static modules.home_tasks.aws_sdk.utils.FileUtils.getPathToPemFile;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RDS Secure Connection Test")
public class RdsSecureConnectionTest extends RdsHooks {

    public static RDSSecureConnector connector;
    private static List<SecureRDSDataReader.ImageMetadata> images;

    private static String endpoint = "http://";
    private static final String imagePath = "src/test/java/modules/home_tasks/aws_sdk/tests/module_5/source/";
    private static final String imageName1 = "JPEG_example.jpg";
    private static final String imageName2 = "SampleJPGImage.jpg";
    private static final SwaggerSteps steps = new SwaggerSteps();

    @BeforeAll
    public static void setUp() {
        System.out.println("Connecting to EC2 instance...");
        connector = new RDSSecureConnector(
                public_dns,
                db_host,
                db_name,
                db_username,
                db_password,
                ec2_username,
                getPathToPemFile()
        );

        endpoint += public_ip + "/api/image";
        System.out.println("Public Host: " + public_ip + "\n" +
                "Endpoint: " + endpoint);
        List<Integer> ids = steps.getImageIds(endpoint);
        if (!ids.isEmpty()) {
            ids.forEach(id -> steps.deleteImageById(String.valueOf(id), endpoint));
        }
    }

    @SneakyThrows
    @Test @Order(1)
    @DisplayName("Verify uploading 2 images and checking info in RDS")
    void verifyImagesInfoInDb() {
//        upload 2 images using Swagger API
        String imageId_1 = steps.uploadImage(imagePath + imageName1, endpoint).jsonPath().getString("id");
        assertTrue(imageId_1.matches("\\d+"));
        String imageId_2 = steps.uploadImage(imagePath + imageName2, endpoint).jsonPath().getString("id");
        assertTrue(imageId_2.matches("\\d+"));
//        get images metadata from RDS
        retrieveData();
        assertEquals(2, images.size(),
                "Images list should have length 2, but was: " + images.size());
//        validate images metadata
        validateImageInfo(images.get(0), imageName1, "83261");
        validateImageInfo(images.get(1), imageName2, "51085");
//       get images metadata from Swagger API
        Response image_api_1 = steps.getImageById(Integer.parseInt(imageId_1), endpoint);
        validateImageInfo(new SecureRDSDataReader.ImageMetadata(
                image_api_1.jsonPath().getString("object_key"),
                image_api_1.jsonPath().getLong("object_size"),
                image_api_1.jsonPath().getString("object_type"),
                image_api_1.jsonPath().getString("last_modified")
        ), imageName1, "83261");
        Response image_api_2 = steps.getImageById(Integer.parseInt(imageId_1), endpoint);
        validateImageInfo(new SecureRDSDataReader.ImageMetadata(
                image_api_2.jsonPath().getString("object_key"),
                image_api_2.jsonPath().getLong("object_size"),
                image_api_2.jsonPath().getString("object_type"),
                image_api_2.jsonPath().getString("last_modified")
        ), imageName2, "51085");
    }

    @SneakyThrows
    @Test @Order(2)
    @DisplayName("Verify deleting all images")
    public void testDeletingAllImages() {
        List<Integer> ids = steps.getImageIds(endpoint);
        if (!ids.isEmpty()) {
            ids.forEach(id -> {
                steps.deleteImageById(String.valueOf(id), endpoint);
//                todo
                steps.getImageById(id, endpoint).then().statusCode(404);
            });
        }
        ids = steps.getImageIds(endpoint);
        assertEquals(ids.size(), 0);

        retrieveData();
        assertEquals(0, images.size(),
                "Images list should have length 0, but was: " + images.size());
    }


    @Step("Retrieve data from RDS")
    private static void retrieveData() throws Exception {
        SecureRDSDataReader reader = new SecureRDSDataReader(connector);
        images = reader.getImageMetadata();
        System.out.println("Found " + images.size() + " images:");
        for (SecureRDSDataReader.ImageMetadata image : images) {
            System.out.println("Image: " + image.getImageKey() + "\n" +
                    "Size: " + image.getSize() + "\n" +
                    "Type: " + image.getImageType() + "\n" +
                    "Modified: " + image.getLastModified() + "\n" + "---");
        }
    }

    @Step("Validate image metadata")
    private void validateImageInfo(SecureRDSDataReader.ImageMetadata image, String imageName, String size) {
        System.out.println("Validating image: " + image.getImageKey());
        assertAll("Image metadata validation",
                () -> assertTrue(image.getImageKey().matches("images/[A-Za-z0-9-]*" + imageName),
                        "Image key should match pattern 'images/[A-Za-z0-9-]*, but was: " + image.getImageKey()),
                () -> assertEquals(size, String.valueOf(image.getSize()),
                        "Image size should be " + size + ", but was: " + image.getSize()),
                () -> assertEquals("binary/octet-stream", image.getImageType(),
                        "Image type should be \"binary/octet-stream\", but was: " + image.getImageType()),
                () -> assertTrue(image.getLastModified().matches(currentDate + " \\d{2}:\\d{2}:\\d{2}"),
                        "Expected last_modified to match pattern '" + currentDate + " \\d{2}:\\d{2}:\\d{2}" +
                                "' but was: " + image.getLastModified())
        );
    }
}
