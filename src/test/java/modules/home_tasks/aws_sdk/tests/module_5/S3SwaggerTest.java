package modules.home_tasks.aws_sdk.tests.module_5;

import io.restassured.response.Response;
import modules.home_tasks.aws_sdk.utils.BaseRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.services.ec2.model.Instance;

import java.util.List;
import static modules.home_tasks.aws_sdk.tests.module_4.Ec2DescriptionUtils.getEc2Instances;
import static modules.home_tasks.aws_sdk.tests.module_4.Ec2DescriptionUtils.getPublicEc2InstanceByName;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("S3 test suite with Swagger")
public class S3SwaggerTest extends BaseRequest {

    private static List<Instance> instances;

    private static String publicHost;
    public static String endpoint = "http://";
    private static String imageId = "";
    private static final String imagePath = "src/test/java/modules/home_tasks/aws_sdk/tests/module_5/source/";
    private static final String imageName = "JPEG_example.jpg";
    private static final SwaggerSteps steps = new SwaggerSteps();

    @BeforeAll
    public static void setUp() {
        instances = getEc2Instances();
        publicHost = getPublicEc2InstanceByName(instances, "cloudximage").publicIpAddress();
        System.out.println("Public Host: " + publicHost);
        endpoint += publicHost + "/api/image";
    }

    @Test @Order(1)
    @DisplayName("Verify GET list of images")
    public void testGettingImages() {
        List<Integer> ids = steps.getImageIds(endpoint);
        assertAll(() -> assertEquals(ids.size(), 0));
    }

    @Test @Order(2)
    @DisplayName("Verify uploading an image")
    public void testUploadingImage() {
        imageId = steps.uploadImage(imagePath + imageName, endpoint).jsonPath().getString("id");
        assertTrue(imageId.matches("\\d+"));
        List<Integer> ids = steps.getImageIds(endpoint);
        assertAll(() -> assertEquals(ids.size(), 1));
        ids.forEach(id -> {
            Response image = steps.getImageById(id, endpoint);
            assertAll(
                    () -> assertEquals(imageId, image.jsonPath().getString("id")),
                    () -> assertTrue(image.jsonPath().getString("last_modified")
                            .matches(currentDate + "T\\d{2}:\\d{2}:\\d{2}Z"),
                            "Expected last_modified to match pattern '" + currentDate + "T\\d{2}:\\d{2}:\\d{2}Z" +
                                    "' but was: " + image.jsonPath().getString("last_modified")),
//                    () -> assertEquals(image.jsonPath().getString("object_key"), "images/" + imageName),
//                    () -> assertTrue(image.jsonPath().getString("object_key").matches("images/[A-Za-z0-9-]{36}-" + imageName),
//                            "Expected object_key to match pattern 'images/[A-Za-z0-9-]{36}-" + imageName +
                    () -> assertTrue(image.jsonPath().getString("object_key").matches("images/[A-Za-z0-9-]*" + imageName),
                            "Expected object_key to match pattern 'images/[A-Za-z0-9-]*" + imageName +
                                    "' but was: " + image.jsonPath().getString("object_key")),
                    () -> assertEquals(image.jsonPath().getInt("object_size"), 83261,
                            "Expected object_size to be 83261 but was: " + image.jsonPath().getInt("object_size")),
                    () -> assertEquals(image.jsonPath().getString("object_type"), "binary/octet-stream",
                            "Expected object_type to be binary/octet-stream but was: " + image.jsonPath().getString("object_type")));
        });
    }

    @Test @Order(3)
    @DisplayName("Verify downloading an image")
    public void testDownloadingImage() {
        Response response = steps.downloadImage("0", 404);
        assertEquals("Image is not found!", response.body().asString());
        response = steps.downloadImage(imageId, 200);
        assertFalse(response.body().asString().isEmpty());
    }

    @Test @Order(4)
    @DisplayName("Verify deleting an image by ID")
    public void testDeletingImageById() {
        Response response = steps.deleteImageById(imageId, endpoint);
        assertTrue(response.body().asString().contains("\"Image is deleted\""));
    }

    @Test @Order(5)
    @DisplayName("Verify deleting all images")
    public void testDeletingAllImages() {
        List<Integer> ids = steps.getImageIds(endpoint);
        if (!ids.isEmpty()) {
            ids.forEach(id -> steps.deleteImageById(String.valueOf(id), endpoint));
        }
        ids = steps.getImageIds(endpoint);
        assertEquals(ids.size(), 0);
    }
}
