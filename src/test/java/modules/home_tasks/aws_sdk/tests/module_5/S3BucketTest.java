package modules.home_tasks.aws_sdk.tests.module_5;

import modules.home_tasks.aws_sdk.tests.module_5.s3.S3Hooks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static modules.home_tasks.BasicHooks.currentDateFormated;
import static modules.home_tasks.aws_sdk.tests.module_5.s3.S3AwsUtils.deleteS3Object;
import static modules.home_tasks.aws_sdk.tests.module_5.s3.S3AwsUtils.listBucketObjects;
import static modules.home_tasks.aws_sdk.tests.module_5.s3.S3AwsUtils.putS3Object;
import static modules.home_tasks.aws_sdk.tests.module_5.s3.S3AwsUtils.writeObjectFromBucketToLocalFile;
import static modules.home_tasks.aws_sdk.utils.FileUtils.readFile;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("S3 SDK actions test")
class S3BucketTest extends S3Hooks {

    private final String imagePath = "src/test/java/modules/home_tasks/aws_sdk/tests/module_5/source/JPEG_example.jpg";
    private final String imagePath_DWN = "src/test/java/modules/home_tasks/aws_sdk/tests/module_5/source/JPEG_example_NEW.jpg";
    private final String imageName = "image_" + currentDateFormated + ".jpg";

    @Test @Order(1)
    @DisplayName("Verify uploading objects in bucket")
    void uploadObjectsInBucketTest() {
        String response = putS3Object(s3Client, bucket_name, "images/" + imageName, imagePath);
        System.out.println(response);
        assertTrue(response.matches("Response eTag: \"[[a-zA-Z_0-9]]{20,40}\""));
    }

    @Test @Order(2)
    @DisplayName("Verify getting list of objects in bucket")
    void getListOfObjectsInBucketTest() {
        listBucketObjects(s3Client, bucket_name);
    }

    @Test @Order(3)
    @DisplayName("Verify downloading objects from bucket")
    void downloadObjectFromBucketTest() {
        writeObjectFromBucketToLocalFile(s3Client, bucket_name, "images/" + imageName, imagePath_DWN);
        assertFalse(readFile(imagePath_DWN).isEmpty());
    }

    @Test @Order(4)
    @DisplayName("Verify deleting objects from bucket")
    void deleteObjectsInBucketTest() {
        deleteS3Object(s3Client, bucket_name, "images/" + imageName);
    }
}
