package modules.home_tasks.aws_sdk.tests.module_3;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeVolumesRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.regex.Pattern.matches;
import static modules.home_tasks.aws_sdk.utils.MessageLogger.logMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("EC2 test suite")
public class Ec2Test {

    static List<Ec2Model> instances = new ArrayList<>();
    static Ec2Client ec2Client;
    private final Ec2Steps steps = new Ec2Steps();

    @Test
    @Order(1)
    @DisplayName("Verify EC2 instances are created")
    public void testInstancesCreated() {
        ec2Client = Ec2Client.builder()
                .region(Region.EU_CENTRAL_1)
                .build();
        steps.findRunningEC2InstancesUsingPaginator(ec2Client);
        instances = instances.stream().sorted(Comparator.comparing(Ec2Model::getName)).collect(Collectors.toList());
        assertAll(
                () -> assertEquals(2, instances.size(),
                        "Expected number of running instances to be 2 but was " + instances.size()),
                () -> assertTrue(instances.get(0).name.contains("PrivateInstance"),
                        "Expected instance name to contain 'PrivateInstance' but was " + instances.get(0).name),
                () -> assertTrue(instances.get(1).name.contains("PublicInstance"),
                        "Expected instance name to contain 'PublicInstance' but was " + instances.get(1).name));
    }

    @Test
    @Order(2)
    @DisplayName("Verify the first EC2 instance")
    public void testInstanceOne() {
        if (instances.isEmpty()) {
            throw new IllegalStateException("No instances found");
        }
        steps.verifyInstance(instances.get(0));
        logMessage("Private instance:\n" + instances.get(0));
        assertNull(instances.get(0).getPublicIp(), "Expected public IP NOT to be present");
    }

    @Test
    @Order(3)
    @DisplayName("Verify the second EC2 instance")
    public void testInstanceTwo() {
        if (instances.isEmpty()) {
            throw new IllegalStateException("Second instance not found");
        }
        steps.verifyInstance(instances.get(1));
        logMessage("Public instance:\n" + instances.get(1));
        assertTrue(matches("\\d+\\.\\d+\\.\\d+\\.\\d+", instances.get(1).getPublicIp()),
                "Expected public IP to be present");
    }

    @Test
    @Order(4)
    @DisplayName("Verify volumes sizes")
    public void testVolumes() {
        List<String> volumeIds = instances.stream().map(Ec2Model::getVolumeId).collect(Collectors.toList());
        if (volumeIds.isEmpty()) {
            throw new IllegalStateException("No volumes found");
        }
        for (String volumeId : volumeIds) {
            DescribeVolumesRequest volumeReq = DescribeVolumesRequest.builder().volumeIds(volumeId).build();
            logMessage("Volume details: " + ec2Client.describeVolumes(volumeReq).volumes().get(0));
            assertEquals(8, ec2Client.describeVolumes(volumeReq).volumes().get(0).size(),
                    "Expected size to be equal to 8, but was "
                            + ec2Client.describeVolumes(volumeReq).volumes().get(0).size());
        }
    }
}
