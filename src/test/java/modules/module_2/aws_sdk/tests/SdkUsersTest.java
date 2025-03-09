package modules.module_2.aws_sdk.tests;

import modules.module_2.BasicHooks;
import modules.module_2.aws_sdk.utils.GetEntitiesLists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.services.iam.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static modules.module_2.aws_sdk.utils.SdkClient.getIamClient;
import static modules.module_2.aws_sdk.utils.SdkClient.iam;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Sdk Users test suite")
public class SdkUsersTest extends BasicHooks {
    private static List<User> users = new ArrayList<>();
    Logger logger = Logger.getLogger(SdkUsersTest.class.getName());

    @Test
    @Order(1) @DisplayName("Verify roles test")
    public void verifyUsers() {
        users = GetEntitiesLists.listUsers(getIamClient());
        assertEquals(4, users.size());
    }

    @Test
    @Order(2) @DisplayName("Verify user cloudx")
    public void verifyUser1() {
        verifyUser(users.get(0), "cloudx", "2025-02-24");
    }

    @Test
    @Order(3) @DisplayName("Verify user FullAccessUserEC2")
    public void verifyUser2() {
        verifyUser(users.get(1), "FullAccessUserEC2", currentDate);
    }

    @Test
    @Order(4) @DisplayName("Verify user FullAccessUserS3")
    public void verifyUser3() {
        verifyUser(users.get(2), "FullAccessUserS3", currentDate);
    }

    @Test
    @Order(5) @DisplayName("Verify user ReadAccessUserS3")
    public void verifyUser4() {
        verifyUser(users.get(3), "ReadAccessUserS3", currentDate);
    }

    private void verifyUser(User user, String userName, String date) {
        logger.info("User: " + user.toString());
        assertAll(
                () -> assertEquals("/", user.path(),
                        "Expected path to be / but was " + user.path()),
                () -> assertEquals(userName, user.userName(),
                        "Expected user name to be " + userName + " but was " + user.userName()),
                () -> assertTrue(user.userId().matches("AIDARZ5BNACF.{9}")
                        , "Expected user ID to match AIDARZ5BNACF followed by 9 characters but was " + user.userId()),
                () -> assertEquals("arn:aws:iam::124355674251:user/" + userName, user.arn(),
                        "Expected ARN to be arn:aws:iam::124355674251:user/" + userName + " but was " + user.arn()),
                () -> assertTrue(user.createDate().toString().matches(date + "T\\d{2}:\\d{2}:\\d{2}Z"),
                        "Expected CreateDate to match " + date + "T\\d{2}:\\d{2}:\\d{2}Z but was " + user.createDate())
        );
    }

    @AfterAll
    public static void cleanUp() {
        iam.close();
    }
}
