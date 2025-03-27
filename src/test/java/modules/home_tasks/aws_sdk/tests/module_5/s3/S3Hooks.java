package modules.home_tasks.aws_sdk.tests.module_5.s3;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.ArrayList;
import java.util.List;

import static modules.home_tasks.aws_sdk.tests.module_5.s3.S3AwsUtils.listBucketsWithDetails;


public class S3Hooks {

    public static S3Client s3Client;
    public static List<String> bucketNames = new ArrayList<>();
    private static final String bucket_name_partial = "cloudximage-imagestorebucket";
    public static String bucket_name;

    @BeforeAll
    public static void setUpClient() {
        s3Client = S3Client.builder()
                .region(Region.EU_CENTRAL_1)
                .build();
        bucketNames = listBucketsWithDetails();
        assert bucketNames != null;
        bucket_name = bucketNames.stream().filter(bucket -> bucket.contains(bucket_name_partial))
                .findFirst().get();
        System.out.println("Bucket name: " + bucket_name);
    }

    @AfterAll
    public static void tearDown() {
        s3Client.close();
        System.out.println("\n============TEST CLASS EXECUTION FINISHED============");
    }
}
