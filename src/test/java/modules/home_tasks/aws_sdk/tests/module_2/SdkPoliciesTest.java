package modules.home_tasks.aws_sdk.tests.module_2;

import modules.home_tasks.BasicHooks;
import modules.home_tasks.aws_sdk.utils.GetEntitiesLists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.policybuilder.iam.IamPolicy;
import software.amazon.awssdk.policybuilder.iam.IamValue;
import software.amazon.awssdk.services.iam.model.GetPolicyVersionResponse;
import software.amazon.awssdk.services.iam.model.Policy;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static modules.home_tasks.PropertyHandler.getProperty;
import static modules.home_tasks.aws_sdk.utils.SdkClient.getIamClient;
import static modules.home_tasks.aws_sdk.utils.SdkClient.iam;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Sdk Policies test suite")
public class SdkPoliciesTest extends BasicHooks {
    private static List<Policy> policies = new ArrayList<>();
    private final Logger logger = Logger.getLogger(SdkPoliciesTest.class.getName());

    @Test
    @Order(1)
    @DisplayName("Verify policies test")
    public void verifyPolicies() {
        policies = GetEntitiesLists.listPolicies(getIamClient()).stream().filter(policy ->
                policy.policyId().contains("ANPARZ5BNACF")).collect(Collectors.toList());
        assertEquals(3, policies.size());
    }

    @Test
    @Order(2) @DisplayName("Verify policy FullAccessPolicyEC2")
    public void verifyPolicy1() {
        verifyPolicy(policies.get(0), "FullAccessPolicyEC2", currentDate);
    }

    @Test
    @Order(3) @DisplayName("Verify policy FullAccessPolicyS3")
    public void verifyPolicy2() {
        verifyPolicy(policies.get(1), "FullAccessPolicyS3", currentDate);
    }

    @Test
    @Order(4) @DisplayName("Verify policy ReadAccessPolicyS3")
    public void verifyPolicy3() {
        verifyPolicy(policies.get(2), "ReadAccessPolicyS3", currentDate);
    }

    @Test
    @Order(5) @DisplayName("Verify policy 1 statement")
    public void verifyPolicy1Statements() {
        GetPolicyVersionResponse getPolicyVersionResponse =
                getIamClient().getPolicyVersion(r -> r.policyArn(policies.get(0).arn()).versionId(getProperty("version")));
        String decodedPolicy = URLDecoder.decode(getPolicyVersionResponse.policyVersion().document(), StandardCharsets.UTF_8);
        IamPolicy iamPolicy = IamPolicy.fromJson(decodedPolicy);
        verifyPolicyStatement(iamPolicy, "[ec2:*]");
    }

    @Test
    @Order(6) @DisplayName("Verify policy 2 statement")
    public void verifyPolicy2Statements() {
        GetPolicyVersionResponse getPolicyVersionResponse =
                getIamClient().getPolicyVersion(r -> r.policyArn(policies.get(1).arn()).versionId(getProperty("version")));
        String decodedPolicy = URLDecoder.decode(getPolicyVersionResponse.policyVersion().document(), StandardCharsets.UTF_8);
        IamPolicy iamPolicy = IamPolicy.fromJson(decodedPolicy);
        verifyPolicyStatement(iamPolicy, "[s3:*]");
    }

    @Test
    @Order(7) @DisplayName("Verify policy 3 statement")
    public void verifyPolicy3Statements() {
        GetPolicyVersionResponse getPolicyVersionResponse =
                getIamClient().getPolicyVersion(r -> r.policyArn(policies.get(2).arn()).versionId(getProperty("version")));
//                getIamClient().getPolicyVersion(r -> r.policyArn(policies.get(2).arn()).versionId("v1"));
        String decodedPolicy = URLDecoder.decode(getPolicyVersionResponse.policyVersion().document(), StandardCharsets.UTF_8);
        IamPolicy iamPolicy = IamPolicy.fromJson(decodedPolicy);
        verifyPolicyStatement(iamPolicy, "[s3:Describe*, s3:Get*, s3:List*]");
    }

    private void verifyPolicyStatement(IamPolicy iamPolicy, String actions) {
        assertAll(
                () -> assertEquals("Allow", iamPolicy.statements().get(0).effect().value(),
                        "Expected effect to be 'Allow' but was " + iamPolicy.statements().get(0).effect().value()),
                () -> assertEquals(actions, iamPolicy.statements().get(0).actions().stream().map(IamValue::value).collect(Collectors.toList()).toString(),
                        "Expected action to be '"+ actions + "' but was " + iamPolicy.statements().get(0).actions().stream().map(IamValue::value).collect(Collectors.toList())),
                () -> assertEquals("*", iamPolicy.statements().get(0).resources().get(0).value(),
                        "Expected resource to be '*' but was " + iamPolicy.statements().get(0).resources().get(0).value()),
                () -> assertEquals(1, iamPolicy.statements().size(),
                        "Expected 1 statement but was " + iamPolicy.statements().size())
        );
    }

    private void verifyPolicy(Policy policy, String policyName, String date) {
        logger.info("Policy: " + policyName);
        assertAll(
                () -> assertEquals("/", policy.path(),
                        "Expected path to be / but was " + policy.path()),
                () -> assertEquals(policyName, policy.policyName(),
                        "Expected policy name to be " + policyName + " but was " + policy.policyName()),
                () -> assertTrue(policy.policyId().matches("ANPARZ5BNACF.{9}"),
                        "Expected policy ID to match ANPARZ5BNACF followed by 9 characters but was " + policy.policyId()),
                () -> assertEquals("arn:aws:iam::124355674251:policy/" + policyName, policy.arn(),
                        "Expected ARN to be arn:aws:iam::124355674251:policy/" + policyName + " but was " + policy.arn()),
                () -> assertTrue(policy.createDate().toString().matches(date + "T\\d{2}:\\d{2}:\\d{2}Z"),
                        "Expected CreateDate to match " + date + "T\\d{2}:\\d{2}:\\d{2}Z but was " + policy.createDate()),
                () -> assertEquals(getProperty("version"), policy.defaultVersionId(),
                        "Expected DefaultVersionId to be " + getProperty("version") + " but was " + policy.defaultVersionId()),
                () -> assertEquals(2, policy.attachmentCount(),
                        "Expected AttachmentCount to be 2 but was " + policy.attachmentCount()),
                () -> assertEquals(0, policy.permissionsBoundaryUsageCount(),
                        "Expected PermissionsBoundaryUsageCount to be 0 but was " + policy.permissionsBoundaryUsageCount()),
                () -> assertEquals("true", policy.isAttachable().toString(),
                        "Expected IsAttachable to be true but was " + policy.isAttachable())
        );
    }

    @AfterAll
    public static void cleanUp() {
        iam.close();
    }
}
