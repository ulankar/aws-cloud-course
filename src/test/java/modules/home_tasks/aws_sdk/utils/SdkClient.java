package modules.home_tasks.aws_sdk.utils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;

import java.io.IOException;

import static modules.home_tasks.PropertyHandler.getProperty;

public class SdkClient {

    public static IamClient iam;

    public static String getAccessKeyId() throws IOException {
        return getProperty("AWS_ACCESS_KEY_ID");
    }

    public static String getSecretAccessKey() throws IOException {
        return getProperty("AWS_SECRET_ACCESS_KEY");
    }

    public static IamClient getIamClient() {
        try {
            iam = IamClient.builder()
                    .region(Region.of(getProperty("REGION")))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(getAccessKeyId(), getSecretAccessKey())
                    ))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return iam;
    }
}
