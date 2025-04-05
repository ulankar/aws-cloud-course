package modules.home_tasks.aws_sdk.tests.module_6;

import io.qameta.allure.Step;
import modules.home_tasks.aws_sdk.utils.AwsHooks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesRequest;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.rds.model.RdsException;
import software.amazon.awssdk.services.rds.model.Tag;

import java.util.List;
import java.util.Map;

import static modules.home_tasks.aws_sdk.tests.module_6.utils.CloudFormationOutputReader.getSpecificOutput;
import static modules.home_tasks.aws_sdk.tests.module_6.utils.CloudFormationOutputReader.getStackOutputs;
import static modules.home_tasks.aws_sdk.tests.module_6.utils.CloudFormationOutputReader.printStackOutputs;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("RDS details verification test")
public class RdsDetailsTest extends AwsHooks {

    private static DBInstance dbInstance;

    @BeforeAll
    public static void setUp() {
        DescribeDbInstancesRequest request = DescribeDbInstancesRequest.builder()
                .dbInstanceIdentifier(db_instance_identifier)
                .build();

        DescribeDbInstancesResponse response = rdsClient.describeDBInstances(request);
        dbInstance = response.dbInstances().get(0);
        describeDBInstances(rdsClient);
    }

    @Test @Order(1)
    @DisplayName("Verify RDS instance type")
    void testInstanceType() {
        assertEquals("db.t3.micro", dbInstance.dbInstanceClass(),
                "Instance type should be db.t3.micro");
    }

    @Test @Order(2)
    @DisplayName("Verify RDS instance multi-AZ configuration")
    void testMultiAZConfiguration() {
        assertFalse(dbInstance.multiAZ(),
                "Multi-AZ should not be enabled");
    }

    @Test @Order(3)
    @DisplayName("Verify RDS instance storage size")
    void testStorageSize() {
        assertEquals(100, dbInstance.allocatedStorage(),
                "Storage size should be 100 GiB");
    }

    @Test @Order(4)
    @DisplayName("Verify RDS instance storage type")
    void testStorageType() {
        assertEquals("gp2", dbInstance.storageType(),
                "Storage type should be gp2");
    }

    @Test @Order(5)
    @DisplayName("Verify RDS instance encryption")
    void testEncryption() {
        assertFalse(dbInstance.storageEncrypted(),
                "Storage encryption should not be enabled");
    }

    @Test @Order(6)
    @DisplayName("Verify RDS instance tags")
    void testInstanceTags() {
        List<Tag> tags = dbInstance.tagList();
        boolean hasExpectedTag = tags.stream()
                .anyMatch(tag -> tag.key().equals("cloudx") &&
                        tag.value().equals("qa"));
        assertTrue(hasExpectedTag,
                "Instance should have tag 'cloudx: qa'");
    }

    @Test @Order(7)
    @DisplayName("Verify RDS instance engine type")
    void testDatabaseType() {
        assertEquals("mysql", dbInstance.engine().toLowerCase(),
                "Database engine should be MySQL");
    }

    @Test @Order(8)
    @DisplayName("Verify RDS instance engine version")
    void testDatabaseVersion() {
        assertEquals("8.0.32", dbInstance.engineVersion(),
                "Database version should be 8.0.32");
    }

    @Test @Order(9)
    @DisplayName("Verify RDS instance availability")
    void testInstanceAvailability() {
        assertEquals("available", dbInstance.dbInstanceStatus().toLowerCase(),
                "Database instance should be available");
    }

//    @Test @Order(10)
//    @DisplayName("Verify RDS instance availability")
//    void testCompleteInstanceConfiguration() {
//        assertAll("RDS Instance Configuration",
//                () -> assertEquals("db.t3.micro", dbInstance.dbInstanceClass()),
//                () -> assertFalse(dbInstance.multiAZ()),
//                () -> assertEquals(100, dbInstance.allocatedStorage()),
//                () -> assertEquals("gp2", dbInstance.storageType()),
//                () -> assertFalse(dbInstance.storageEncrypted()),
//                () -> assertTrue(dbInstance.tagList().stream()
//                        .anyMatch(tag -> tag.key().equals("cloudx") &&
//                                tag.value().equals("qa"))),
//                () -> assertEquals("mysql", dbInstance.engine().toLowerCase()),
//                () -> assertEquals("8.0.32", dbInstance.engineVersion())
//        );
//    }

    @Test @Order(11)
    @DisplayName("Verify RDS instance output values")
    void testOutputValues() {
        printStackOutputs(stackName);
        String specificOutput = getSpecificOutput(stackName, "ImageBucketName");
        System.out.println("ImageBucketName: " + specificOutput);

        Map<String, String> allOutputs = getStackOutputs(stackName);
        allOutputs.forEach((key, value) -> {
            if (!key.endsWith("_Description")) {
                System.out.println(key + " = " + value);
            }
        });
    }

    @Step("Describe DB instances")
    public static void describeDBInstances(RdsClient rdsClient) {
        try {
            DescribeDbInstancesResponse response = rdsClient.describeDBInstances();
            List<DBInstance> dbInstances = response.dbInstances();

            if (dbInstances.isEmpty()) {
                System.out.println("No DB instances found.");
                return;
            }

            System.out.println("Found " + dbInstances.size() + " DB instance(s):");

            for (DBInstance instance : dbInstances) {
                System.out.println("\n=== DB Instance Details ===");
                System.out.println("Instance Identifier: " + instance.dbInstanceIdentifier());
                System.out.println("Instance Class: " + instance.dbInstanceClass());
                System.out.println("Engine: " + instance.engine());
                System.out.println("Engine Version: " + instance.engineVersion());
                System.out.println("Status: " + instance.dbInstanceStatus());
                System.out.println("Allocated Storage: " + instance.allocatedStorage() + " GB");

                if (instance.endpoint() != null) {
                    System.out.println("Endpoint: " + instance.endpoint().address());
                    System.out.println("Port: " + instance.endpoint().port());
                }

                System.out.println("db.t3.micro " + instance.dbInstanceClass());
                System.out.println("Multi-AZ: " + instance.multiAZ());
                System.out.println("Allocated storage: " + instance.allocatedStorage());
                System.out.println("Storage type: " + instance.storageType());
                System.out.println("Storage encrypted: " + instance.storageEncrypted());
                System.out.println("Publicly accessible: " + instance.publiclyAccessible());
                System.out.println("'cloudx' tag: " + instance.tagList().stream()
                        .anyMatch(tag -> tag.key().equals("cloudx")));

                System.out.println("Engine: " + instance.engine());
                System.out.println("Engine version: " + instance.engineVersion());

                if (instance.dbName() != null) {
                    System.out.println("Database Name: " + instance.dbName());
                }

                if (instance.availabilityZone() != null) {
                    System.out.println("Availability Zone: " + instance.availabilityZone());
                }
            }

        } catch (RdsException e) {
            System.err.println("Error describing DB instances: " + e.getMessage());
            System.exit(1);
        }
    }
}
