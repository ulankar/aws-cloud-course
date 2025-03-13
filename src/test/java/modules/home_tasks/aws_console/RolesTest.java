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

public class RolesTest {

    private final String command = "aws iam list-roles --query Roles[?contains(RoleName,`Access`)]";
    private JsonNode responseJson;
        private final String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

    @Test
    public void verifyRoles() throws IOException {
        String response = RunScript.runConsoleCommand(command);

        responseJson = new ObjectMapper().readTree(response);
        JsonNode role1 = responseJson.get(0);
        JsonNode role2 = responseJson.get(1);
        JsonNode role3 = responseJson.get(2);

        assertEquals(3, responseJson.size());
        verifyRole(role1, "FullAccessRoleEC2", date, "ec2.amazonaws.com");
        verifyRole(role2, "FullAccessRoleS3", date, "ec2.amazonaws.com");
        verifyRole(role3, "ReadAccessRoleS3", date, "ec2.amazonaws.com");
    }

    private void verifyRole(JsonNode role, String roleName, String date, String principal) {
        assertAll(
                () -> assertEquals("/", role.get("Path").asText(),
                        "Expected the path to be '/' but was " + role.get("Path")),
                () -> assertEquals(roleName, role.get("RoleName").asText(),
                        "Expected the role name to be " + roleName + " but was " + role.get("RoleName")),
                () -> assertTrue(role.get("RoleId").asText().matches("AROARZ5BNACF.{9}"),
                        "Expected the role ID to match the pattern AROARZ5BNACF.{9} but was " + role.get("RoleId")),
                () -> assertEquals("arn:aws:iam::124355674251:role/" + roleName, role.get("Arn").asText(),
                        "Expected the ARN to be arn:aws:iam::124355674251:role/" + roleName + " but was " + role.get("Arn")),
                () -> assertTrue(role.get("CreateDate").asText().matches(date + "T\\d{2}:\\d{2}:\\d{2}\\+00:00"),
                        "Expected the create date to match the pattern " + date + "T\\d{2}:\\d{2}:\\d{2}\\+00:00 but was " + role.get("CreateDate")),
                () -> assertEquals("2012-10-17", role.get("AssumeRolePolicyDocument").get("Version").asText(),
                        "Expected the version to be 2012-10-17 but was " + role.get("AssumeRolePolicyDocument").get("Version")),
                () -> assertEquals("Allow", role.get("AssumeRolePolicyDocument").get("Statement").get(0).get("Effect").asText(),
                        "Expected the effect to be 'Allow' but was " + role.get("AssumeRolePolicyDocument").get("Statement").get(0).get("Effect")),
                () -> assertEquals(principal, role.get("AssumeRolePolicyDocument").get("Statement").get(0).get("Principal").get("Service").asText(),
                        "Expected the principal to be " + principal + " but was " + role.get("AssumeRolePolicyDocument").get("Statement").get(0).get("Principal").get("Service")),
                () -> assertEquals("sts:AssumeRole", role.get("AssumeRolePolicyDocument").get("Statement").get(0).get("Action").asText(),
                        "Expected the action to be 'sts:AssumeRole' but was " + role.get("AssumeRolePolicyDocument").get("Statement").get(0).get("Action")),
                () -> assertEquals("", role.get("Description").asText(),
                        "Expected the description to be '' but was " + role.get("Description")),
                () -> assertEquals("3600", role.get("MaxSessionDuration").asText(),
                        "Expected the max session duration to be '3600' but was " + role.get("MaxSessionDuration")),
                () -> assertEquals("", role.get("Description").asText(),
                        "Expected the description to be '' but was " + role.get("Description")),
                () -> assertEquals("3600", role.get("MaxSessionDuration").asText(),
                        "Expected the max session duration to be '3600' but was " + role.get("MaxSessionDuration"))
        );
    }
}
