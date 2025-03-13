package modules.module_2.aws_console;

import modules.module_2.aws_console.utils.RunScript;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UsersTest {

    private final String command = "aws iam list-users";
    private JsonNode responseJson;
    private final String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

    @Test
    public void verifyUsers() throws IOException {
        String response = RunScript.runConsoleCommand(command);

        responseJson = new ObjectMapper().readTree(response);
        JsonNode users = responseJson.get("Users");
        JsonNode user1 = users.get(0);
        JsonNode user2 = users.get(1);
        JsonNode user3 = users.get(2);
        JsonNode user4 = users.get(3);
        assertEquals(4, users.size());

        verifyUser(user1, "cloudx", "2025-02-24");
        verifyUser(user2, "FullAccessUserEC2", date);
        verifyUser(user3, "FullAccessUserS3", date);
        verifyUser(user4, "ReadAccessUserS3", date);
    }

    private void verifyUser(JsonNode user, String userName, String date) {
        assertAll(
                () -> assertEquals("/", user.get("Path").asText()),
                () -> assertEquals(userName, user.get("UserName").asText()),
                () -> assertTrue(user.get("UserId").asText().matches("AIDARZ5BNACF.{9}")),
                () -> assertEquals("arn:aws:iam::124355674251:user/" + userName, user.get("Arn").asText()),
                () -> assertTrue(user.get("CreateDate").asText().matches(date + "T\\d{2}:\\d{2}:\\d{2}\\+00:00"))
        );
    }
}
