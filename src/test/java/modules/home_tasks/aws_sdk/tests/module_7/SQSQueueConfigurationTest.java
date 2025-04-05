package modules.home_tasks.aws_sdk.tests.module_7;

import modules.home_tasks.aws_sdk.utils.AwsHooks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.model.*;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SQS Queue Configuration Test")
public class SQSQueueConfigurationTest extends AwsHooks {

    @Test
    @DisplayName("Verify SQS queue configuration")
    public void testQueueConfiguration() {
        ListQueuesRequest listQueuesRequest = ListQueuesRequest.builder()
                .queueNamePrefix(queueNamePrefix)
                .build();
        ListQueuesResponse listQueuesResponse = sqsClient.listQueues(listQueuesRequest);
        String targetQueueUrl = listQueuesResponse.queueUrls().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Queue not found"));
        System.out.println("Found queue URL: " + targetQueueUrl);

        GetQueueAttributesRequest getQueueAttributesRequest = GetQueueAttributesRequest.builder()
                .queueUrl(targetQueueUrl)
                .attributeNames(QueueAttributeName.ALL)
                .build();
        GetQueueAttributesResponse queueAttributes = sqsClient.getQueueAttributes(getQueueAttributesRequest);

        String queueName = targetQueueUrl.substring(targetQueueUrl.lastIndexOf("/") + 1);
        System.out.println("\nQueue Configuration:");
        System.out.println("Queue Name: " + queueName);

        boolean isFifoQueue = queueAttributes.attributes().get(QueueAttributeName.FIFO_QUEUE) != null &&
                Boolean.parseBoolean(queueAttributes.attributes().get(QueueAttributeName.FIFO_QUEUE));
        System.out.println("Queue Type: " + (isFifoQueue ? "FIFO" : "Standard"));

        String kmsKeyId = queueAttributes.attributes().get(QueueAttributeName.KMS_MASTER_KEY_ID);
        String sseEnabled = queueAttributes.attributes().get(QueueAttributeName.SQS_MANAGED_SSE_ENABLED);
        boolean isEncrypted = (kmsKeyId != null && !kmsKeyId.isEmpty()) || sseEnabled.equals("true");
        System.out.println("Encryption: " + (isEncrypted ? "Enabled" : "Disabled"));

        String deadLetterQueueArn = queueAttributes.attributes().get(QueueAttributeName.REDRIVE_POLICY);
        boolean hasDeadLetterQueue = deadLetterQueueArn != null && !deadLetterQueueArn.isEmpty();
        System.out.println("Dead-letter queue: " + (hasDeadLetterQueue ? "Yes" : "No"));
        ListQueueTagsRequest listTagsRequest = ListQueueTagsRequest.builder()
                .queueUrl(targetQueueUrl)
                .build();
        ListQueueTagsResponse tagsResponse = sqsClient.listQueueTags(listTagsRequest);
        System.out.println("\nTags:");
        if (tagsResponse.tags() != null) {
            tagsResponse.tags().forEach((key, value) -> {
                System.out.println(key + ": " + value);
            });
        }

        assertAll(
                () -> assertTrue(queueName.startsWith("cloudximage-QueueSQSQueue"),
                        "Queue name should match pattern 'cloudximage-QueueSQSQueue' but was: " + queueName),
                () -> assertFalse(isFifoQueue,
                        "Queue should be standard but was FIFO"),
                () -> assertTrue(isEncrypted,
                        "Encryption should be enabled but was not"),
                () -> assertFalse(hasDeadLetterQueue,
                        "Dead-letter queue should not be present but was found"),
                () -> assertEquals("qa", tagsResponse.tags().get("cloudx"),
                        "Tag 'cloudx' should have value 'qa' but was: " + tagsResponse.tags().get("cloudx"))
        );
    }
}
