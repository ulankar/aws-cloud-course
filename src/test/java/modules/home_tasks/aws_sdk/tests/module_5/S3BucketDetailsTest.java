package modules.home_tasks.aws_sdk.tests.module_5;

import io.qameta.allure.Step;
import modules.home_tasks.aws_sdk.tests.module_5.s3.S3Hooks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.services.s3.model.PublicAccessBlockConfiguration;

import java.util.Objects;

import static modules.home_tasks.aws_sdk.utils.MessageLogger.logMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("S3 bucket details test")
public class S3BucketDetailsTest extends S3Hooks {

    @Test
    @Order(1)
    @DisplayName("Verify bucket encryption")
    public void testEncryption() {
        checkBucketEncryption(bucket_name);
    }

    @Test
    @Order(2)
    @DisplayName("Verify bucket versioning")
    public void testVersioning() {
        checkBucketVersioning(bucket_name);
    }

    @Test
    @Order(3)
    @DisplayName("Verify bucket's public access")
    public void testPublicAccess() {
        checkBucketPublicAccess(bucket_name);
    }

    @Test
    @Order(4)
    @DisplayName("Verify bucket's tag")
    public void testTag() {
        getBucketTag(bucket_name);
    }

    @Step("Get bucket description")
    private void checkBucketEncryption(String bucketName) {
        String encryption = s3Client.getBucketEncryption(b -> b.bucket(bucketName))
                .serverSideEncryptionConfiguration()
                .rules().get(0)
                .applyServerSideEncryptionByDefault()
                .sseAlgorithm().toString();
        logMessage("Bucket encryption: " + encryption);
        if (encryption.equals("AES256")) {
            System.out.println("Bucket is using SSE-S3 encryption");
        }
        assertEquals("AES256", encryption,
                "Bucket is not using SSE-S3 encryption, as expected algorithm to be 'AES256' but was " + encryption);
    }

    @Step("Check bucket versioning")
    private void checkBucketVersioning(String bucketName) {
        String versioning = String.valueOf(s3Client.getBucketVersioning(b -> b.bucket(bucketName)).status());
        if (Objects.equals(versioning, "null") || versioning.equals("Suspended")) {
            System.out.println("Bucket versioning is not enabled");
        }
        assertEquals("null", versioning,
                "Bucket versioning is enabled, as expected versioning status to be 'null' but was " + versioning);
    }

    @Step("Check bucket public access")
    private void checkBucketPublicAccess(String bucketName) {
        PublicAccessBlockConfiguration publicAccess = s3Client.getPublicAccessBlock(b -> b.bucket(bucketName))
                .publicAccessBlockConfiguration();
        logMessage(
                "PublicAccessBlockConfiguration details: " +
                        " \n- blockPublicAcls: '" + publicAccess.blockPublicAcls() +
                        "'\n- ignorePublicAcls: '" + publicAccess.ignorePublicAcls() +
                        "'\n- blockPublicPolicy: '" + publicAccess.blockPublicPolicy() +
                        "'\n- restrictPublicBuckets: '" + publicAccess.restrictPublicBuckets() + "'");
        assertAll(() -> assertEquals(true, publicAccess.blockPublicAcls(),
                        "Expected 'blockPublicAcls' to be 'true' but was " + publicAccess.blockPublicAcls()),
                () -> assertEquals(true, publicAccess.ignorePublicAcls(),
                        "Expected 'ignorePublicAcls' to be 'true' but was " + publicAccess.ignorePublicAcls()),
                () -> assertEquals(true, publicAccess.blockPublicPolicy(),
                        "Expected 'blockPublicPolicy' to be 'true' but was " + publicAccess.blockPublicPolicy()),
                () -> assertEquals(true, publicAccess.restrictPublicBuckets(),
                        "Expected 'restrictPublicBuckets' to be 'true' but was " + publicAccess.restrictPublicBuckets()));
    }

    @Step("Get bucket tag")
    private void getBucketTag(String bucketName) {
        String tag = s3Client.getBucketTagging(b -> b.bucket(bucketName)).tagSet().stream()
                .filter(t -> t.key().equals("cloudx")).findFirst().get().value();
        System.out.println("Tag: " + tag);
        assertEquals("qa", tag, "Expected tag to be 'cloudx' but was " + tag);
    }
}
