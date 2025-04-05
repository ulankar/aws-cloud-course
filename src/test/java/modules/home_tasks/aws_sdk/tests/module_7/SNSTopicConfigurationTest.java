package modules.home_tasks.aws_sdk.tests.module_7;

import modules.home_tasks.aws_sdk.utils.AwsHooks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sns.model.*;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SNS Topic Configuration Test")
public class SNSTopicConfigurationTest extends AwsHooks {

    @Test
    @DisplayName("Verify SNS topic configuration")
    public void testTopicConfiguration() {
        System.out.println("Topic ARN: " + topicArn);
        GetTopicAttributesRequest getTopicAttributesRequest = GetTopicAttributesRequest.builder()
                .topicArn(topicArn)
                .build();
        GetTopicAttributesResponse topicAttributes = snsClient.getTopicAttributes(getTopicAttributesRequest);

        System.out.println("\nTopic Configuration:");
        String topicName = topicArn.substring(topicArn.lastIndexOf(":") + 1);
        System.out.println("Topic Name: " + topicName);

        boolean isFifoTopic = topicAttributes.attributes().containsKey("FifoTopic") &&
                Boolean.parseBoolean(topicAttributes.attributes().get("FifoTopic"));
        System.out.println("Topic Type: " + (isFifoTopic ? "FIFO" : "Standard"));

        boolean isEncrypted = topicAttributes.attributes().containsKey("KmsMasterKeyId") &&
                !topicAttributes.attributes().get("KmsMasterKeyId").isEmpty();
        System.out.println("Encryption: " + (isEncrypted ? "Enabled" : "Disabled"));

        ListTagsForResourceRequest listTagsRequest = ListTagsForResourceRequest.builder()
                .resourceArn(topicArn)
                .build();
        ListTagsForResourceResponse tagsResponse = snsClient.listTagsForResource(listTagsRequest);
        System.out.println("\nTags:");
        for (Tag tag : tagsResponse.tags()) {
            System.out.println(tag.key() + ": " + tag.value());
        }

        assertAll(
                () -> assertTrue(topicName.startsWith("cloudximage-TopicSNSTopic"),
                        "Topic name should match pattern 'cloudximage-TopicSNSTopic' but was: " + topicName),
                () -> assertFalse(isFifoTopic,
                        "Topic should be 'Standard' but was 'FIFO'"),
                () -> assertFalse(isEncrypted,
                        "Encryption should be disabled but was not"),
                () -> assertEquals("qa", tagsResponse.tags().stream()
                                .filter(tag -> tag.key().equals("cloudx")).findFirst().get().value(),
                        "Tag 'cloudx' should have value 'qa' but was: " + tagsResponse.tags().stream()
                                .filter(tag -> tag.key().equals("cloudx")).findFirst().get().value())
        );
    }
}
