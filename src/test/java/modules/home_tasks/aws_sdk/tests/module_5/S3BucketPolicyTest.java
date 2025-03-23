package modules.home_tasks.aws_sdk.tests.module_5;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Step;
import lombok.SneakyThrows;
import modules.home_tasks.aws_sdk.tests.module_5.s3.S3Hooks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.services.s3.model.GetBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.GetBucketPolicyResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static modules.home_tasks.aws_sdk.utils.MessageLogger.logMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("S3 bucket policy test")
public class S3BucketPolicyTest extends S3Hooks {

    private static String policy;

    @BeforeAll
    public static void setUp() {
        policy = getBucketPolicy(bucket_name);
    }

    @Test
    @Order(1)
    @DisplayName("Verify bucket policy")
    public void testBucketPolicy() {
        displayBucketPolicyDetails(bucket_name);
        verifyBucketPolicy(policy);
    }

    @SneakyThrows
    @Step("Verify bucket policy")
    private void verifyBucketPolicy(String policy) {
        JsonNode version = new ObjectMapper().readTree(policy).get("Version");
        JsonNode statements = new ObjectMapper().readTree(policy).get("Statement");
        logMessage("Bucket policy version: " + version.asText());
        logMessage("Bucket policy statements: " + statements.toString());

        assertAll(
                () -> assertEquals(1, statements.size(),
                        "Expected number of statement to be 1, but found " + statements.size()),
                () -> assertEquals("2012-10-17", version.asText()),
                () -> assertEquals("Allow", statements.get(0).get("Effect").asText()),
                () -> assertEquals(4, statements.get(0).get("Action").size()),
                () -> assertEquals("s3:DeleteObject*", statements.get(0).get("Action").get(0).asText()),
                () -> assertEquals("s3:GetBucket*", statements.get(0).get("Action").get(1).asText()),
                () -> assertEquals("s3:List*", statements.get(0).get("Action").get(2).asText()),
                () -> assertEquals("s3:PutBucketPolicy", statements.get(0).get("Action").get(3).asText()),
                () -> assertTrue(statements.get(0).get("Principal").get("AWS").asText().matches(
                        "arn:aws:iam::\\d{12}:role/cloudximage-CustomS3AutoDeleteObjectsCustomResource-.*")),
                () -> assertTrue(statements.get(0).get("Resource").get(0).asText()
                                .matches("arn:aws:s3:::cloudximage-imagestorebucket.*"),
                        "Resource should match pattern 'arn:aws:s3:::cloudximage-imagestorebucket.*', but was: " +
                                statements.get(0).get("Resource").get(0).asText()),
                () -> assertTrue(statements.get(0).get("Resource").get(1).asText()
                                .matches("arn:aws:s3:::cloudximage-imagestorebucket.*/\\*"),
                        "Resource should match pattern 'arn:aws:s3:::cloudximage-imagestorebucket.*/\\*', but was: " +
                                statements.get(0).get("Resource").get(1).asText()));
    }

    @Step("Get bucket policy")
    public static String getBucketPolicy(String bucketName) {
        try {
            GetBucketPolicyRequest policyRequest = GetBucketPolicyRequest.builder()
                    .bucket(bucketName)
                    .build();
            GetBucketPolicyResponse response = s3Client.getBucketPolicy(policyRequest);
            return response.policy();
        } catch (S3Exception e) {
            System.err.println("Error getting bucket policy: " + e.getMessage());
            throw e;
        }
    }

    @Step("Display bucket policy details")
    public void displayBucketPolicyDetails(String bucketName) {
        try {
            if (policy != null) {
                System.out.println("Bucket policy found for: " + bucketName);
                System.out.println("Policy content:");
                System.out.println(policy);

                ObjectMapper mapper = new ObjectMapper();
                JsonNode policyJson = mapper.readTree(policy);
                JsonNode statements = policyJson.get("Statement");
                if (statements.isArray()) {
                    System.out.println("----------------------------------------------------------");
                    for (JsonNode statement : statements) {
                        System.out.println("Effect: " + statement.get("Effect").asText());

                        JsonNode actions = statement.get("Action");
                        if (actions.isArray()) {
                            System.out.println("Actions:");
                            for (JsonNode action : actions) {
                                System.out.println("  - " + action.asText());
                            }
                        } else {
                            System.out.println("Action: " + actions.asText());
                        }

                        if (statement.has("Principal")) {
                            System.out.println("Principal: " + statement.get("Principal").toString());
                        }

                        if (statement.has("Resource")) {
                            System.out.println("Resource: " + statement.get("Resource").toString());
                        }
                        System.out.println("----------------------------------------------------------");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing bucket policy: " + e.getMessage());
        }
    }

    public boolean hasPolicyWithPublicAccess(String bucketName) {
        try {
            String policy = getBucketPolicy(bucketName);
            if (policy == null) {
                return false;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode policyJson = mapper.readTree(policy);
            JsonNode statements = policyJson.get("Statement");

            for (JsonNode statement : statements) {
                JsonNode principal = statement.get("Principal");
                if (principal != null) {
                    if (principal.isTextual() && principal.asText().equals("*")) {
                        return true;
                    }
                    if (principal.isObject() && principal.has("AWS")) {
                        JsonNode aws = principal.get("AWS");
                        if (aws.isTextual() && aws.asText().equals("*")) {
                            return true;
                        }
                        if (aws.isArray()) {
                            for (JsonNode arn : aws) {
                                if (arn.asText().equals("*")) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error checking for public access: " + e.getMessage());
            return false;
        }
    }
}
