package modules.module_2.aws_sdk.tests;

import modules.module_2.BasicHooks;
import modules.module_2.aws_sdk.utils.GetEntitiesLists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.services.iam.model.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static modules.module_2.aws_sdk.utils.SdkClient.getIamClient;
import static modules.module_2.aws_sdk.utils.SdkClient.iam;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Sdk Roles test suite")
public class SdkRolesTest extends BasicHooks {
    private static List<Role> roles = new ArrayList<>();
    Logger logger = Logger.getLogger(SdkRolesTest.class.getName());

    @Test
    @Order(1) @DisplayName("Verify roles test")
    public void verifyRoles() {
        roles = GetEntitiesLists.listRoles(getIamClient()).stream().filter(role ->
                role.assumeRolePolicyDocument().contains("ec2.amazonaws.com")).collect(Collectors.toList());
        logger.info(roles.get(0).assumeRolePolicyDocument());

        assertEquals(3, roles.size());
    }

    @Test
    @Order(2) @DisplayName("Verify role FullAccessRoleEC2")
    public void verifyRole1() {
        verifyRole(roles.get(0), "FullAccessRoleEC2", currentDate);
    }

    @Test
    @Order(3) @DisplayName("Verify role FullAccessRoleS3")
    public void verifyRole2() {
        verifyRole(roles.get(1), "FullAccessRoleS3", currentDate);
    }

    @Test
    @Order(3) @DisplayName("Verify role ReadAccessRoleS3")
    public void verifyRole3() {
        verifyRole(roles.get(2), "ReadAccessRoleS3", currentDate);
    }

    private void verifyRole(Role role, String roleName, String date) {
        System.out.println("Role: " + role.toString());
        assertAll(
                () -> assertEquals("/", role.path()),
                () -> assertEquals(roleName, role.roleName()),
                () -> assertTrue(role.roleId().matches("AROARZ5BNACF.{9}")),
                () -> assertEquals("arn:aws:iam::124355674251:role/" + roleName, role.arn()),
                () -> assertTrue(role.createDate().toString().matches(date + "T\\d{2}:\\d{2}:\\d{2}Z")),
                () -> assertTrue(role.assumeRolePolicyDocument().contains("2012-10-17")),
                () -> assertTrue(role.assumeRolePolicyDocument().contains("Allow")),
                () -> assertTrue(role.assumeRolePolicyDocument().contains("ec2.amazonaws.com")),
                () -> assertTrue(role.assumeRolePolicyDocument().contains("AssumeRole")),
                () -> assertEquals("", role.description()),
                () -> assertEquals(3600, role.maxSessionDuration())
        );
    }

    @AfterAll
    public static void cleanUp() {
        iam.close();
    }
}
