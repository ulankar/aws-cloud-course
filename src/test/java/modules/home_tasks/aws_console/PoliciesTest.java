package modules.home_tasks.aws_console;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import modules.home_tasks.aws_console.utils.RunScript;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PoliciesTest {

    private final String command = "aws iam list-policies --scope Local";
    private JsonNode responseJson;
    private final String version = "v1";
    private final String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

    @Test
    public void verifyPolicies() throws IOException {
        String response = RunScript.runConsoleCommand(command);

        responseJson = new ObjectMapper().readTree(response);
        JsonNode policies = responseJson.get("Policies");
        JsonNode policy1 = policies.get(0);
        JsonNode policy2 = policies.get(1);
        JsonNode policy3 = policies.get(2);
        assertEquals(3, policies.size());

        String policy1Name = policy1.get("PolicyName").asText();
        String policy2Name = policy2.get("PolicyName").asText();

        if (policy1Name.equals("FullAccessPolicyEC2")) {
            verifyPolicy(policy1, "FullAccessPolicyEC2");
            if (policy2Name.equals("FullAccessPolicyS3")) {
                verifyPolicy(policy2, "FullAccessPolicyS3");
                verifyPolicy(policy3, "ReadAccessPolicyS3");
            } else {
                verifyPolicy(policy2, "ReadAccessPolicyS3");
                verifyPolicy(policy3, "FullAccessPolicyS3");
            }
        } else if (policy1Name.equals("FullAccessPolicyS3")) {
            verifyPolicy(policy1, "FullAccessPolicyS3");
            if (policy2Name.equals("FullAccessPolicyEC2")) {
                verifyPolicy(policy2, "FullAccessPolicyEC2");
                verifyPolicy(policy3, "ReadAccessPolicyS3");
            } else {
                verifyPolicy(policy2, "ReadAccessPolicyS3");
                verifyPolicy(policy3, "FullAccessPolicyEC2");
            }
        } else {
            verifyPolicy(policy1, "ReadAccessPolicyS3");
            if (policy2Name.equals("FullAccessPolicyEC2")) {
                verifyPolicy(policy2, "FullAccessPolicyEC2");
                verifyPolicy(policy3, "FullAccessPolicyS3");
            } else {
                verifyPolicy(policy2, "FullAccessPolicyS3");
                verifyPolicy(policy3, "FullAccessPolicyEC2");
            }
        }
    }

    private void verifyPolicy(JsonNode policy, String policyName) {
        assertAll(
                () -> assertEquals(policyName, policy.get("PolicyName").asText()),
                () -> assertTrue(policy.get("PolicyId").asText().matches("ANPARZ5BNACF.{9}")),
                () -> assertEquals("arn:aws:iam::124355674251:policy/" + policyName, policy.get("Arn").asText()),
                () -> assertEquals("/", policy.get("Path").asText()),
                () -> assertEquals(version, policy.get("DefaultVersionId").asText()),
                () -> assertEquals("2", policy.get("AttachmentCount").asText()),
                () -> assertEquals("0", policy.get("PermissionsBoundaryUsageCount").asText()),
                () -> assertEquals("true", policy.get("IsAttachable").asText()),
                () -> assertTrue(policy.get("CreateDate").asText().matches(date + "T.*"))
        );
    }
}
