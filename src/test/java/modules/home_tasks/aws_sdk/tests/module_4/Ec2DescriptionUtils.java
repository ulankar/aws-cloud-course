package modules.home_tasks.aws_sdk.tests.module_4;

import io.qameta.allure.Step;
import modules.home_tasks.aws_sdk.utils.BaseRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeKeyPairsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeKeyPairsResponse;
import software.amazon.awssdk.services.ec2.model.Instance;

import java.util.List;
import java.util.stream.Collectors;

public class Ec2DescriptionUtils extends BaseRequest {

    static Ec2Client ec2Client;

    @Step("Get EC2 instances")
    public static List<Instance> getEc2Instances() {
        ec2Client = Ec2Client.builder()
                .region(Region.EU_CENTRAL_1)
                .build();
        DescribeInstancesRequest describeInstancesRequest = DescribeInstancesRequest.builder()
                .filters(f -> f.name("instance-state-name").values("running"))
                .build();
        return ec2Client.describeInstances(describeInstancesRequest).reservations().stream()
                .flatMap(reservation -> reservation.instances().stream()).collect(Collectors.toList());
    }

    @Step("Get private EC2 instance")
    public static Instance getPrivateEc2Instance(List<Instance> instances) {
        return instances.stream().filter(inst -> inst.tags().stream()
                .filter(tag -> tag.key().equals("Name")).findFirst().get().value()
                .contains("Private")).findFirst().get();
    }

    @Step("Get public EC2 instance")
    public static Instance getPublicEc2Instance(List<Instance> instances) {
        return instances.stream().filter(inst -> inst.tags().stream()
                .filter(tag -> tag.key().equals("Name")).findFirst().get().value()
                .contains("Public")).findFirst().get();
    }

    @Step("Get EC2 instance by name")
    public static Instance getPublicEc2InstanceByName(List<Instance> instances, String name) {
        return instances.stream().filter(inst -> inst.tags().stream()
                .filter(tag -> tag.key().equals("Name")).findFirst().get().value()
                .contains(name)).findFirst().get();
    }

    @Step("Get EC2 key name")
    public static String getEc2KeyName() {
        String keyName = getEc2Instances().get(0).keyName();
        DescribeKeyPairsRequest request = DescribeKeyPairsRequest.builder()
                .keyNames(keyName)
                .build();
        DescribeKeyPairsResponse response = ec2Client.describeKeyPairs(request);
        System.out.println("\nKey Pair Details: " + response.keyPairs().get(0));
        return response.keyPairs().get(0).keyPairId();
    }
}
