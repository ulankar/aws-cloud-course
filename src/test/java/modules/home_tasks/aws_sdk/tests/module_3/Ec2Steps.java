package modules.home_tasks.aws_sdk.tests.module_3;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Instance;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Ec2Steps {

    Logger logger = Logger.getLogger(Ec2Steps.class.getName());

    void verifyInstance(Ec2Model instance) {
        logger.info("Instance ID: " + instance.getInstanceId());
        assertAll(
                () -> assertEquals("running", instance.getState(),
                        "Expected state to be 'running' but was " + instance.getState()),
                () -> assertEquals("t2.micro", instance.getInstanceType(),
                        "Expected instance type to be 't2.micro' but was " + instance.getInstanceType()),
                () -> assertEquals("qa", instance.getTag(),
                        "Expected tag to be 'qa' but was " + instance.getTag()),
                () -> assertEquals("Linux/UNIX", instance.getPlatform(),
                        "Expected platform to be 'Linux/UNIX' but was " + instance.getPlatform()),
                () -> assertEquals("qa", instance.getTag(),
                        "Expected tag to be 'qa' but was " + instance.getTag())
        );
    }

    void findRunningEC2InstancesUsingPaginator(Ec2Client ec2Client) {
        DescribeInstancesRequest describeInstancesRequest = DescribeInstancesRequest.builder()
                .filters(f -> f.name("instance-state-name").values("running"))
                .build();
        List<Instance> instances = ec2Client.describeInstances(describeInstancesRequest).reservations().stream()
                .flatMap(reservation -> reservation.instances().stream()).collect(Collectors.toList());
        for (Instance instance : instances) {
            Ec2Model model = new Ec2Model(
                    instance.instanceId(),
                    instance.state().name().toString(),
                    instance.instanceType().toString(),
                    instance.tags().stream().filter(tag -> tag.key().equals("cloudx")).findFirst().get().value(),
                    instance.publicIpAddress(),
                    instance.platformDetails(),
                    instance.tags().stream().filter(tag -> tag.key().equals("Name")).findFirst().get().value(),
                    instance.blockDeviceMappings().get(0).ebs().volumeId()
            );
            Ec2Test.instances.add(model);
        }
    }
}
