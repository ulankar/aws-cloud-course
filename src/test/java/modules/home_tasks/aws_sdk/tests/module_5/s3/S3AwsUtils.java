package modules.home_tasks.aws_sdk.tests.module_5.s3;

import io.qameta.allure.Step;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static modules.home_tasks.aws_sdk.tests.module_5.s3.S3Hooks.bucketNames;
import static modules.home_tasks.aws_sdk.tests.module_5.s3.S3Hooks.s3Client;
import static modules.home_tasks.aws_sdk.utils.MessageLogger.logMessage;

public class S3AwsUtils {

    @Step("List buckets with details")
    public static List<String> listBucketsWithDetails() {
        try {
            ListBucketsResponse response = s3Client.listBuckets();
            List<Bucket> buckets = response.buckets();

            if (buckets.isEmpty()) {
                System.out.println("No buckets found in your account.");
                return null;
            }
            System.out.println("Found " + buckets.size() + " buckets:");

            for (Bucket bucket : buckets) {
                printBucketDetails(bucket);
                bucketNames.add(bucket.name());
            }
        } catch (Exception e) {
            System.err.println("Error listing buckets: " + e.getMessage());
        }
        return bucketNames;
    }

    @Step("Print bucket details")
    private static void printBucketDetails(Bucket bucket) {
        String bucketName = bucket.name();
        Instant creationDate = bucket.creationDate();
        logMessage("Bucket Name: " + bucketName +
                "\nCreation Date: " + creationDate);
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());
            System.out.println("Bucket Status: Accessible");

            String region = s3Client.getBucketLocation(b -> b.bucket(bucketName))
                    .locationConstraintAsString();
            System.out.println("Region: " + region);
        } catch (Exception e) {
            System.out.println("Bucket Status: Not accessible - " + e.getMessage());
        }
    }

    @Step("List bucket objects")
    public static void listBucketObjects(S3Client s3, String bucketName) {
        try {
            ListObjectsRequest listObjects = ListObjectsRequest
                    .builder()
                    .bucket(bucketName)
                    .build();
            ListObjectsResponse res = s3.listObjects(listObjects);
            List<S3Object> objects = res.contents();
            for (S3Object item : objects) {
                logMessage(item.toString());
                System.out.print("\n The name of the key is " + item.key());
                System.out.print("\n The object is " + bytesToKBytes(item.size()) + " KBs");
                System.out.print("\n The owner is " + item.owner());
            }
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
    }

    @Step("Read object from bucket")
    private static long bytesToKBytes(long val) {
        return val / 1024;
    }

    @Step("Read object from bucket")
    public static String readObjectFromBucketAsString(S3Client s3, String bucketName, String keyName) {
        String stringFromBucket = "";
        try {
            byte[] data = getBytesFromBucket(s3, bucketName, keyName);

            stringFromBucket = new String(data, StandardCharsets.UTF_8);
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            e.printStackTrace();
        }
        return stringFromBucket;
    }

    @Step("Get bytes from bucket")
    private static byte[] getBytesFromBucket(S3Client s3, String bucketName, String keyName) {
        GetObjectRequest objectRequest = GetObjectRequest
                .builder()
                .bucket(bucketName)
                .key(keyName)
                .build();
        ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
        return objectBytes.asByteArray();
    }

    @Step("Write object from bucket to local file")
    public static void writeObjectFromBucketToLocalFile(S3Client s3, String bucketName, String keyName, String path) {
        try {
            byte[] data = getBytesFromBucket(s3, bucketName, keyName);
            // Write the data to a local file.
            File myFile = new File(path);
            OutputStream os = Files.newOutputStream(myFile.toPath());
            os.write(data);
            System.out.println("Successfully obtained bytes from an S3 object");
            os.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Step("Put object to bucket")
    public static String putS3Object(S3Client s3, String bucketName, String objectKey, String objectPath) {
        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("x-amz-meta-myVal", "test");
            PutObjectRequest putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .metadata(metadata)
                    .build();
            PutObjectResponse response = s3.putObject(putOb, RequestBody.fromBytes(getObjectFile(objectPath)));
            return "Response eTag: " + response.eTag();
        } catch (S3Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    @Step("Delete object from bucket")
    public static void deleteS3Object(S3Client s3, String bucketName, String objectKey) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
        s3.deleteObject(deleteObjectRequest);
    }

    @Step("Get object file")
    private static byte[] getObjectFile(String filePath) {
        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;
        try {
            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytesArray;
    }
}
