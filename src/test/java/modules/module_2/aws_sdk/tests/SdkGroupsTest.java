package modules.module_2.aws_sdk.tests;

import modules.module_2.BasicHooks;
import modules.module_2.aws_sdk.utils.GetEntitiesLists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.services.iam.model.Group;

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
@DisplayName("Sdk Groups test suite")
public class SdkGroupsTest extends BasicHooks {
    private static List<Group> groups = new ArrayList<>();
    Logger logger = Logger.getLogger(SdkGroupsTest.class.getName());

    @Test
    @Order(1) @DisplayName("Verify groups test")
    public void verifyGroups() {
        groups = GetEntitiesLists.listGroups(getIamClient()).stream().filter(group ->
                group.groupName().contains("AccessGroup")).collect(Collectors.toList());
        assertEquals(3, groups.size());
    }

    @Test
    @Order(2) @DisplayName("Verify group FullAccessGroupEC2")
    public void verifyGroup1() {
        verifyGroup(groups.get(0), "FullAccessGroupEC2", currentDate);
    }

    @Test
    @Order(3) @DisplayName("Verify group FullAccessGroupS3")
    public void verifyGroup2() {
        verifyGroup(groups.get(1), "FullAccessGroupS3", currentDate);
    }

    @Test
    @Order(4) @DisplayName("Verify group ReadAccessGroupS3")
    public void verifyGroup3() {
        verifyGroup(groups.get(2), "ReadAccessGroupS3", currentDate);
    }

    private void verifyGroup(Group group, String groupName, String date) {
        logger.info("Group: " + group.toString());
        assertAll(
                () -> assertEquals("/", group.path(),
                        "Expected path to be / but was " + group.path()),
                () -> assertEquals(groupName, group.groupName(),
                        "Expected group name to be " + groupName + " but was " + group.groupName()),
                () -> assertTrue(group.groupId().matches("AGPARZ5BNACF.{9}")
                        , "Expected group ID to match AIDARZ5BNACF followed by 9 characters but was " + group.groupId()),
                () -> assertEquals("arn:aws:iam::124355674251:group/" + groupName, group.arn(),
                        "Expected ARN to be arn:aws:iam::124355674251:group/" + groupName + " but was " + group.arn()),
                () -> assertTrue(group.createDate().toString().matches(date + "T\\d{2}:\\d{2}:\\d{2}Z"),
                        "Expected CreateDate to match " + date + "T\\d{2}:\\d{2}:\\d{2}Z but was " + group.createDate())
        );
    }

    @AfterAll
    public static void cleanUp() {
        iam.close();
    }
}
