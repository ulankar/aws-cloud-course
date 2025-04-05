package modules.home_tasks.aws_sdk.tests.module_7;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import modules.home_tasks.aws_sdk.utils.ApiSteps;
import modules.home_tasks.aws_sdk.utils.AwsHooks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.services.sns.model.*;

import javax.mail.Folder;
import javax.mail.Message;
import java.util.Map;

import static modules.home_tasks.PropertyHandler.getProperty;
import static modules.home_tasks.aws_sdk.utils.EmailHandler.checkEmailNotReceived;
import static modules.home_tasks.aws_sdk.utils.EmailHandler.getEmail;
import static modules.home_tasks.aws_sdk.utils.EmailHandler.getTextFromMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("SNS email subscription test")
public class SNSEmailSubscriptionTest extends AwsHooks {

    private static String subscriptionArn;
    private static String subscriptionEmailMessage;
    private static String uploadEmailMessage;
    private static String deletedEmailMessage;
    private static String token;
    private static String imageId;
    private static String endpoint;

    private static final String imagePath = "src/test/java/modules/home_tasks/aws_sdk/tests/module_5/source/";
    private static final String imageName = "JPEG_example.jpg";
    private static final ApiSteps steps = new ApiSteps();

    @Test
    @Order(1)
    @DisplayName("Subscribe to SNS topic with email")
    void testSnsEmailSubscription() {
        subscribeToNotificationsWithEmail();
        subscriptionEmailMessage = readEmail("AWS Notification - Subscription Confirmation");
        checkEmailContentSubscriptionConfirmation(subscriptionEmailMessage);
        getToken();
        confirmSubscription(topicArn, token);
        listSubscriptions();
    }

    @Test
    @Order(2)
    @DisplayName("Notification on image upload")
    void testNotificationOnImageUpload() {
        uploadImage();
        uploadEmailMessage = readEmail("AWS Notification Message");
        checkEmailContentImageUpload(uploadEmailMessage);
        emailWasNotReceived("AWS Notification Message");
    }

    @Test
    @Order(3)
    @DisplayName("Notification on image delete")
    void testNotificationOnImageDelete() {
        deleteImage();
        deletedEmailMessage = readEmail("AWS Notification Message");
        checkEmailContentImageDeleted(deletedEmailMessage);
        emailWasNotReceived("AWS Notification Message");
    }

    @Test
    @Order(4)
    @DisplayName("Unsubscribe from SNS topic")
    void testUnsubscription() {
        unsubscribe(subscriptionArn);
        int subs = listSubscriptions();
        System.out.println("Number of subscriptions: " + subs);
    }

    @Test
    @Order(5)
    @DisplayName("Email is not received after unsubscribtion")
    void testNoEmailsAfterUnsubscription() {
        uploadImage();
        emailWasNotReceived("AWS Notification Message");
    }

    @Test
    @Order(6)
    @DisplayName("Subscribe to SNS topic with email")
    void testSnsEmailSubscriptionWithApi() {
        steps.createSubscription();
        subscriptionEmailMessage = readEmail("AWS Notification - Subscription Confirmation");
        checkEmailContentSubscriptionConfirmation(subscriptionEmailMessage);
        getToken();
        confirmSubscription(topicArn, token);
        steps.deleteSubscription();
    }


    @Step("Check email content - Image deleted")
    private void checkEmailContentImageDeleted(String emailMessage) {
        String imageUrl = "http://ec2-" + public_ip.replaceAll("\\.", "-") +
                ".eu-central-1.compute.amazonaws.com/api/image/file/" + imageId;
        assertAll(
                () -> assertTrue(emailMessage.contains("event_type: delete"), "Should contain 'event_type: delete' but was not"),
                () -> assertTrue(emailMessage.contains("object_key: images/"), "Should contain 'object_key: images/' but was not"),
                () -> assertTrue(emailMessage.contains("-JPEG_example.jpg"), "Should contain '-JPEG_example.jpg' but was not"),
                () -> assertTrue(emailMessage.contains("object_type: binary/octet-stream"), "Should contain 'object_type: binary/octet-stream' but was not"),
                () -> assertTrue(emailMessage.contains("last_modified: "), "Should contain 'last_modified: ' but was not"),
                () -> assertTrue(emailMessage.contains("object_size: 83261"), "Should contain 'object_size: 83261' but was not"),
                () -> assertTrue(emailMessage.contains("download_link: "), "Should contain 'download_link: ' but was not"),
                () -> assertFalse(emailMessage.contains(imageUrl), "Should NOT contain '" + imageUrl + "' but was"),
                () -> assertTrue(emailMessage.contains("If you wish to stop receiving notifications from this topic, " +
                                "please click or visit the link below to unsubscribe"),
                        "Should contain 'If you wish to stop receiving notifications from this topic, please click or visit the link below to unsubscribe' but was not"),
                () -> assertTrue(emailMessage.contains("https://sns.eu-central-1.amazonaws.com/unsubscribe.html?SubscriptionArn=" +
                        subscriptionArn + "&Endpoint=turnir.iproaction.test@gmail.com"), "Should contain 'https://sns.eu-central-1.amazonaws.com/unsubscribe.html?SubscriptionArn=" +
                        subscriptionArn + "&Endpoint=turnir.iproaction.test@gmail.com' but was not"),
                () -> assertTrue(emailMessage.contains("Please do not reply directly to this email. " +
                                "If you have any questions or comments regarding this email, please contact us at https://aws.amazon.com/support"),
                        "Should contain 'Please do not reply directly to this email. If you have any questions or comments regarding this email, please contact us at https://aws.amazon.com/support' but was not")
        );
    }

    @Step("Check email content - Subscription confirmation")
    private void checkEmailContentSubscriptionConfirmation(String emailMessage) {
        assertAll(
                () -> assertTrue(emailMessage.contains("You have chosen to subscribe to the topic:"),
                        "Should contain 'You have chosen to subscribe to the topic:' but was not"),
                () -> assertTrue(emailMessage.contains(topicArn), "Should contain '" + topicArn + "' but was not"),
                () -> assertTrue(emailMessage.contains("To confirm this subscription, click or visit the link below " +
                        "(If this was in error no action is necessary):"), "Should contain 'To confirm this subscription, click or visit the link below (If this was in error no action is necessary):' but was not"),
                () -> assertTrue(emailMessage.contains("Please do not reply directly to this email. " +
                                "If you wish to remove yourself from receiving all future SNS subscription confirmation requests " +
                                "please send an email to <a href=\"mailto:sns-opt-out@amazon.com\">sns-opt-out"),
                        "Should contain 'Please do not reply directly to this email. If you wish to remove yourself from receiving all future SNS subscription confirmation requests " +
                                "please send an email to <a href=\"mailto:sns-opt-out@amazon.com\">sns-opt-out' but was not")
        );
    }

    @Step("Upload image")
    private static void uploadImage() {
        endpoint = "http://" + public_ip + "/api/image";
        imageId = steps.uploadImage(imagePath + imageName, endpoint).jsonPath().getString("id");
    }

    @Step("Delete image")
    private static void deleteImage() {
        endpoint = "http://" + public_ip + "/api/image";
        steps.deleteImageById(imageId, endpoint);
    }

    @Step("Check email content - Image uploaded")
    private void checkEmailContentImageUpload(String emailMessage) {
        String imageUrl = "http://ec2-" + public_ip.replaceAll("\\.", "-") +
                ".eu-central-1.compute.amazonaws.com/api/image/file/" + imageId;
        assertAll(
                () -> assertTrue(emailMessage.contains("event_type: upload"), "Should contain 'event_type: upload' but was not"),
                () -> assertTrue(emailMessage.contains("object_key: images/"), "Should contain 'object_key: images/' but was not"),
                () -> assertTrue(emailMessage.contains("-JPEG_example.jpg"), "Should contain '-JPEG_example.jpg' but was not"),
                () -> assertTrue(emailMessage.contains("object_type: binary/octet-stream"), "Should contain 'object_type: binary/octet-stream' but was not"),
                () -> assertTrue(emailMessage.contains("last_modified: 2025-"), "Should contain 'last_modified: 2025-' but was not"),
                () -> assertTrue(emailMessage.contains("object_size: 83261"), "Should contain 'object_size: 83261' but was not"),
                () -> assertTrue(emailMessage.contains("download_link: " + imageUrl), "Should contain 'download_link: " + imageUrl + "' but was not"),
                () -> assertTrue(emailMessage.contains("If you wish to stop receiving notifications from this topic, " +
                        "please click or visit the link below to unsubscribe"), "Should contain 'If you wish to stop receiving notifications from this topic, " +
                        "please click or visit the link below to unsubscribe' but was not"),
                () -> assertTrue(emailMessage.contains("https://sns.eu-central-1.amazonaws.com/unsubscribe.html?SubscriptionArn=" +
                        subscriptionArn + "&Endpoint=turnir.iproaction.test@gmail.com"), "Should contain 'https://sns.eu-central-1.amazonaws.com/unsubscribe.html?SubscriptionArn=" +
                        subscriptionArn + "&Endpoint=turnir.iproaction.test@gmail.com' but was not"),
                () -> assertTrue(emailMessage.contains("Please do not reply directly to this email. " +
                                "If you have any questions or comments regarding this email, please contact us at https://aws.amazon.com/support"),
                        "Should contain 'Please do not reply directly to this email. If you have any questions or comments regarding this email, please contact us at https://aws.amazon.com/support' but was not")
        );
    }

    @Step("Get token from email message")
    private static void getToken() {
        token = subscriptionEmailMessage.split("&Token=")[1].split("&Endpoint=" + emailAddress)[0];
    }

    @SneakyThrows
    @Step("Read email message")
    private static String readEmail(String subject) {
        Map<Message, Folder> emailMessageAndFolder = getEmail(getProperty("GMAIL_BOX"),
                getProperty("GMAIL_PASSWORD"), subject);
        String email = getTextFromMessage(emailMessageAndFolder.keySet().iterator().next());
        System.out.println("The text of the message is:\n" + email);
        return email;
    }

    @SneakyThrows
    @Step("Check email was not received")
    private static void emailWasNotReceived(String subject) {
        boolean noEmailReceived = checkEmailNotReceived(getProperty("GMAIL_BOX"),
                getProperty("GMAIL_PASSWORD"), subject);
        assertTrue(noEmailReceived, "Email was not received");
    }

    @Step("Confirm subscription")
    public void confirmSubscription(String topicArn, String token) {
        ConfirmSubscriptionRequest request = ConfirmSubscriptionRequest.builder()
                .topicArn(topicArn)
                .token(token)
                .build();

        ConfirmSubscriptionResponse response = snsClient.confirmSubscription(request);
        subscriptionArn = response.subscriptionArn();
        System.out.println("Subscription confirmed. Subscription ARN: " + subscriptionArn);
    }

    @Step("Subscribe to notifications with email")
    private static void subscribeToNotificationsWithEmail() {
        SubscribeRequest subscribeRequest = SubscribeRequest.builder()
                .topicArn(topicArn)
                .protocol("email")
                .endpoint(emailAddress)
                .returnSubscriptionArn(true)
                .build();

        SubscribeResponse response = snsClient.subscribe(subscribeRequest);

        System.out.println("Subscription ARN: " + response.subscriptionArn());
        System.out.println("Please check your email and confirm the subscription.");

        if (!response.subscriptionArn().equals("pending confirmation")) {
            GetSubscriptionAttributesRequest attributesRequest = GetSubscriptionAttributesRequest.builder()
                    .subscriptionArn(response.subscriptionArn())
                    .build();

            GetSubscriptionAttributesResponse attributesResponse =
                    snsClient.getSubscriptionAttributes(attributesRequest);

            System.out.println("\nSubscription Details:");
            System.out.println("Protocol: " + attributesResponse.attributes().get("Protocol"));
            System.out.println("Endpoint: " + attributesResponse.attributes().get("Endpoint"));
            System.out.println("Status: " + attributesResponse.attributes().get("PendingConfirmation"));
        }
    }

    @Step("List subscriptions")
    private static int listSubscriptions() {
        ListSubscriptionsByTopicRequest request = ListSubscriptionsByTopicRequest.builder()
                .topicArn(topicArn)
                .build();
        ListSubscriptionsByTopicResponse result = snsClient.listSubscriptionsByTopic(request);

        if (result.subscriptions().isEmpty()) {
            System.out.println("No subscriptions found for the topic.");
            return 0;
        }

        System.out.println("\nExisting subscriptions for the topic (by AWS SDK):");
        result.subscriptions().forEach(subscription -> {
            System.out.println("Subscription ARN: " + subscription.subscriptionArn());
            System.out.println("Owner: " + subscription.owner());
            System.out.println("Protocol: " + subscription.protocol());
            System.out.println("Endpoint: " + subscription.endpoint());
            System.out.println("Topic ARN: " + subscription.topicArn());
            System.out.println("--------------------");
        });

        System.out.println("\nExisting subscriptions for the topic (by API):");
        Response response = steps.getAllSubscriptions();
        System.out.println("SubscriptionArn: " + response.jsonPath().getList("SubscriptionArn").get(0));
        System.out.println("Owner: " + response.jsonPath().getList("Owner").get(0));
        System.out.println("Protocol: " + response.jsonPath().getList("Protocol").get(0));
        System.out.println("Endpoint: " + response.jsonPath().getList("Endpoint").get(0));
        System.out.println("TopicArn: " + response.jsonPath().getList("TopicArn").get(0));
        return result.subscriptions().size();
    }

    @Step("Unsubscribe")
    public static void unsubscribe(String subscriptionArn) {
        UnsubscribeRequest request = UnsubscribeRequest.builder()
                .subscriptionArn(subscriptionArn)
                .build();
        snsClient.unsubscribe(request);
        System.out.println("Successfully unsubscribed");
    }
}
