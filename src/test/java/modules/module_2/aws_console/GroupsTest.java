package modules.module_2.aws_console;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import modules.module_2.aws_console.utils.RunScript;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GroupsTest {

    private final String command = "aws iam list-groups";
    private static JsonNode responseJson;
    private final String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

    @Test
    @Order(1)
    public void verifyUserGroups() throws IOException {
        String response = RunScript.runConsoleCommand(command);
        responseJson = new ObjectMapper().readTree(response);
        JsonNode groups = responseJson.get("Groups");
        assertEquals(4, groups.size());
    }

    @Test
    @Order(2)
    public void verifyUserGroup1() {
        JsonNode group1 = responseJson.get("Groups").get(1);
        verifyGroup(group1, "FullAccessGroupEC2");
    }

    @Test
    @Order(3)
    public void verifyUserGroup2() {
        JsonNode group2 = responseJson.get("Groups").get(2);
        verifyGroup(group2, "FullAccessGroupS3");
    }

    @Test
    @Order(4)
    public void verifyUserGroup3() {
        JsonNode group3 = responseJson.get("Groups").get(3);
        verifyGroup(group3, "ReadAccessGroupS3");
    }

    private void verifyGroup(JsonNode group, String groupName) {
        assertAll(
                () -> assertEquals("/", group.get("Path").asText()),
                () -> assertEquals(groupName, group.get("GroupName").asText()),
                () -> assertTrue(group.get("GroupId").asText().matches("AGPARZ5BNACF.{9}")),
                () -> assertEquals("arn:aws:iam::124355674251:group/" + groupName, group.get("Arn").asText()),
                () -> assertTrue(group.get("CreateDate").asText().matches(date + "T.*"))
        );
    }
}
