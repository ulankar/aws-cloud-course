package modules.home_tasks.aws_sdk.tests.module_4;

import io.qameta.allure.Step;
import modules.home_tasks.BasicHooks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("VPC details validation tests")
public class VpcDetailsTests extends BasicHooks {

    private static Ec2Client ec2Client;
    private static Vpc vpc;
    private static List<SecurityGroup> securityGroups;
    private static List<Subnet> subnets;

    @Test
    @Order(1)
    @DisplayName("Verify VPC test")
    void testVpc() {
        ec2Client = Ec2Client.builder().build();
        describeVpcs();
        verifyVpc(vpc, "10.0.0.0/16", "qa");
    }

    @Test
    @Order(2)
    @DisplayName("Verify Subnet 1 test")
    void testSubnet1() {
        subnets = getDescribeSubnets(vpc.vpcId());
        assertEquals(2, subnets.size(),
                "Expected number of subnets to be 2 but was " + subnets.size());
        verifySubnets(subnets.stream().filter(x -> x.cidrBlock().contains("10.0.128.0")).findFirst().get(),
                "10.0.128.0/17");
    }

    @Test
    @Order(3)
    @DisplayName("Verify Subnet 2 test")
    void testSubnet2() {
        verifySubnets(subnets.stream().filter(x -> x.cidrBlock().contains("10.0.0.0")).findFirst().get(),
                "10.0.0.0/17");
    }

    @Test
    @Order(4)
    @DisplayName("Verify RouteTables test")
    void testRouteTables() {
        List<RouteTable> routeTables = getDescribeRouteTables(vpc.vpcId());
        assertEquals(3, routeTables.size(),
                "Expected number of route tables to be 3 but was " + routeTables.size());
    }

    @Test
    @Order(5)
    @DisplayName("Verify SecurityGroups test")
    void testSecurityGroups() {
        List<SecurityGroup> securityGroups = getDescribeSecurityGroups(vpc.vpcId());
        assertEquals(3, securityGroups.size(),
                "Expected number of security groups to be 3 but was " + securityGroups.size());
        SecurityGroup defaultSecurityGroup = securityGroups.stream().filter(x -> x.groupName().contains("default")).findFirst().get();
        SecurityGroup publicSecurityGroup = securityGroups.stream().filter(x -> x.groupName().contains("cloudxinfo-Public")).findFirst().get();
        SecurityGroup privateSecurityGroup = securityGroups.stream().filter(x -> x.groupName().contains("cloudxinfo-Private")).findFirst().get();
        verifySecurityGroups(defaultSecurityGroup, "default", "default VPC security group", 1);
        verifySecurityGroups(publicSecurityGroup, "cloudxinfo-PublicInstanceSecurityGroup", "cloudxinfo/PublicInstance/SecurityGroup", 2);
        verifySecurityGroups(privateSecurityGroup, "cloudxinfo-PrivateInstanceSecurityGroup", "cloudxinfo/PrivateInstance/SecurityGroup", 2);
    }

    @Test
    @Order(6)
    @DisplayName("Verify SecurityGroups Inbound Rules for Default security group test")
    void testSecurityGroupsInboundRulesDefault() {
        securityGroups = getDescribeSecurityGroups(vpc.vpcId());
        SecurityGroup defaultSecurityGroup = securityGroups.stream().filter(x -> x.groupName().contains("default")).findFirst().get();
        verifyIpPermissions(defaultSecurityGroup.ipPermissions().get(0), "-1", "null - null", "[]");
    }

    @Test
    @Order(7)
    @DisplayName("Verify SecurityGroups Inbound Rules for Public security group test")
    void testSecurityGroupsInboundRulesPublic() {
        SecurityGroup publicSecurityGroup = securityGroups.stream().filter(x -> x.groupName().contains("cloudxinfo-Public")).findFirst().get();
        verifyIpPermissions(publicSecurityGroup.ipPermissions().get(0), "tcp", "80 - 80",
                "[IpRange(Description=HTTP from Internet, CidrIp=0.0.0.0/0)]");
        verifyIpPermissions(publicSecurityGroup.ipPermissions().get(1), "tcp", "22 - 22",
                "[IpRange(Description=SSH from Internet, CidrIp=0.0.0.0/0)]");
    }

    @Test
    @Order(8)
    @DisplayName("Verify SecurityGroups Inbound Rules for Private security group IP permission 1 test")
    void testSecurityGroupsInboundRulesPrivate1() {
        SecurityGroup privateSecurityGroup = securityGroups.stream().filter(x -> x.groupName().contains("cloudxinfo-Private")).findFirst().get();
        verifyIpPermissions(privateSecurityGroup.ipPermissions().get(0), "tcp", "80 - 80", "[]");
    }

    @Test
    @Order(9)
    @DisplayName("Verify SecurityGroups Inbound Rules for Private security group IP permission 2 test")
    void testSecurityGroupsInboundRulesPrivate2() {
        SecurityGroup privateSecurityGroup = securityGroups.stream().filter(x -> x.groupName().contains("cloudxinfo-Private")).findFirst().get();
        verifyIpPermissions(privateSecurityGroup.ipPermissions().get(1), "tcp", "22 - 22", "[]");
    }

    @Step("Verify Inbound Rules")
    private void verifyIpPermissions(IpPermission ipPermission, String protocol, String portRange, String ipRanges) {
        assertAll(
                () -> assertEquals(protocol, ipPermission.ipProtocol(),
                        "Expected ipProtocol to be " + protocol + " but was " + ipPermission.ipProtocol()),
                () -> assertEquals(portRange, ipPermission.fromPort() + " - " + ipPermission.toPort(),
                        "Expected port range to be " + portRange + " but was " + ipPermission.fromPort() + " - " + ipPermission.toPort()),
                () -> assertEquals(ipRanges, ipPermission.ipRanges().toString(),
                        "Expected ipRanges to be " + ipRanges + " but was " + ipPermission.ipRanges().toString())
        );
    }

    @Step("Verify Security Groups")
    private void verifySecurityGroups(SecurityGroup securityGroup, String name, String description, int permissions) {
        assertAll(
                () -> assertTrue(securityGroup.groupId().matches("sg-[a-z0-9]{17}"),
                        "Expected group ID to match pattern 'sg-[a-z0-9]{17}' but was " + securityGroup.groupId()),
                () -> assertTrue(securityGroup.groupName().contains(name),
                        "Expected group name to contain " + name + " but was " + securityGroup.groupName()),
                () -> assertEquals(description, securityGroup.description(),
                        "Expected group description to be " + description + " but was " + securityGroup.description()),
                () -> assertEquals(permissions, securityGroup.ipPermissions().size(),
                        "Expected group ipPermissions size to be " + permissions + " but was " + securityGroup.ipPermissions().size())
        );
    }

    @Step("Describe VPCs")
    public void describeVpcs() {
        DescribeVpcsRequest request = DescribeVpcsRequest.builder().build();
        DescribeVpcsResponse response = ec2Client.describeVpcs(request);
        vpc = response.vpcs().stream()
                .filter(v -> v.tags().stream()
                        .anyMatch(x -> x.key().equals("Name") && x.value().contains("cloudxinfo")))
                .findFirst().orElseThrow();
        System.out.println("\n=== VPC Information ===" +
                "\nVPC ID: " + vpc.vpcId() +
                "\nState: " + vpc.stateAsString() +
                "\nCIDR Block: " + vpc.cidrBlock() +
                "\nIs Default VPC: " + vpc.isDefault());

        if (!vpc.tags().isEmpty()) {
            System.out.println("Tags:");
            vpc.tags().forEach(tag -> {
                        System.out.println("  " + tag.key() + ": " + tag.value());
                    }
            );
        }
    }

    @Step("Verify VPC")
    private void verifyVpc(Vpc vpc, String cidrBlock, String tag) {
        assertAll(
                () -> assertEquals(cidrBlock, vpc.cidrBlock(),
                        "Expected CIDR block to be " + cidrBlock + " but was " + vpc.cidrBlock()),
                () -> assertEquals(tag, vpc.tags().stream().filter(x -> x.key().equals("cloudx")).findFirst().get().value(),
                        "Expected 'cloudx' tag to be " + tag)
        );
    }

    @Step("Describe Subnets")
    private List<Subnet> getDescribeSubnets(String vpcId) {
        DescribeSubnetsRequest request = DescribeSubnetsRequest.builder()
                .filters(Filter.builder()
                        .name("vpc-id")
                        .values(vpcId)
                        .build())
                .build();
        DescribeSubnetsResponse response = ec2Client.describeSubnets(request);
        List<Subnet> subnets = response.subnets();
        System.out.println("\nSubnets in VPC " + vpcId + ":");
        for (Subnet subnet : subnets) {
            System.out.println("  Subnet ID: " + subnet.subnetId() +
                    "\n  CIDR Block: " + subnet.cidrBlock() +
                    "\n  Availability Zone: " + subnet.availabilityZone() +
                    "\n  Available IP Count: " + subnet.availableIpAddressCount());
        }
        return subnets;
    }

    @Step("Verify Subnets")
    private void verifySubnets(Subnet subnet, String cidrBlock) {
        assertAll(
                () -> assertTrue(subnet.subnetId().matches("subnet-[a-z0-9]{17}"),
                        "Expected subnetId to match pattern 'subnet-[a-z0-9]{17}' but was " + subnet.subnetId()),
                () -> assertEquals(cidrBlock, subnet.cidrBlock(),
                        "Expected CIDR block to be " + cidrBlock + " but was " + subnet.cidrBlock()),
                () -> assertEquals("eu-central-1a", subnet.availabilityZone(),
                        "Expected availability zone to be 'eu-central-1a' but was " + subnet.availabilityZone()),
                () -> assertTrue(subnet.availableIpAddressCount().toString().matches("\\d{5}"),
                        "Expected available IP count to match pattern '\\d{5}' but was " + subnet.availableIpAddressCount())
        );
    }

    @Step("Describe Route Tables")
    private List<RouteTable> getDescribeRouteTables(String vpcId) {
        DescribeRouteTablesRequest request = DescribeRouteTablesRequest.builder()
                .filters(Filter.builder()
                        .name("vpc-id")
                        .values(vpcId)
                        .build())
                .build();
        DescribeRouteTablesResponse response = ec2Client.describeRouteTables(request);

        List<RouteTable> routeTables = response.routeTables();
        System.out.println("\nRoute Tables in VPC " + vpcId + ":");
        for (RouteTable routeTable : routeTables) {
            String name = routeTable.tags().stream()
                    .filter(tag -> tag.key().equals("Name"))
                    .map(Tag::value)
                    .findFirst()
                    .orElse("No name tag found");

            System.out.println("  Route Table ID: " + routeTable.routeTableId() +
                    "\n  Route Table associations: " + routeTable.associations() +
                    "\n  Name: " + name);
            System.out.println("  Routes:");
            for (Route route : routeTable.routes()) {
                System.out.println("    Destination: " + route.destinationCidrBlock() +
                        "\n    Target: " + route.gatewayId());
            }
        }
        return routeTables;
    }

    @Step("Describe Security Groups")
    private List<SecurityGroup> getDescribeSecurityGroups(String vpcId) {
        DescribeSecurityGroupsRequest request = DescribeSecurityGroupsRequest.builder()
                .filters(Filter.builder()
                        .name("vpc-id")
                        .values(vpcId)
                        .build())
                .build();
        DescribeSecurityGroupsResponse response = ec2Client.describeSecurityGroups(request);

        List<SecurityGroup> securityGroups = response.securityGroups();
        System.out.println("\nSecurity Groups in VPC " + vpcId + ":");
        for (SecurityGroup securityGroup : securityGroups) {
            System.out.println("  Security Group ID: " + securityGroup.groupId() +
                    "\n  Name: " + securityGroup.groupName() +
                    "\n  Description: " + securityGroup.description());
            System.out.println("  Inbound Rules:");
            for (IpPermission permission : securityGroup.ipPermissions()) {
                System.out.println("    Protocol: " + permission.ipProtocol() +
                        "\n    Port Range: " + permission.fromPort() + " - " + permission.toPort() +
                        "\n    IP Ranges: " + permission.ipRanges());
            }
        }
        return securityGroups;
    }

    private void verifyRoutes(Route route, String destination, String target) {
//        verifyRouteTables(routeTables.get(0), "rtb-00e1037184a590d16", false);
//
//        List<Route> routes = routeTables.get(0).routes();
//        assertEquals(2, routes.size(),
//                "Expected number of routes to be 2 but was " + routes.size());
//        routes = routes.stream().sorted(Comparator.comparing(Route::destinationCidrBlock)).collect(Collectors.toList());
//        verifyRoutes(routes.get(0), "0.0.0.0/0", null);
//        verifyRoutes(routes.get(1), "10.0.0.0/16", "local");
//
//        verifyRouteTables(routeTables.get(1), "rtb-01345a92c3e7abe41", true);
//        List<Route> routes_2 = routeTables.get(1).routes();
//        assertEquals(1, routes_2.size(),
//                "Expected number of routes to be 1 but was " + routes_2.size());
//        verifyRoutes(routes_2.get(0), "10.0.0.0/16", "local");
//
//        verifyRouteTables(routeTables.get(2), "rtb-0d69386cfd6a6c299", false);
//        List<Route> routes_3 = routeTables.get(2).routes();
//        assertEquals(2, routes_3.size(),
//                "Expected number of routes to be 2 but was " + routes_3.size());
//        routes_3 = routes_3.stream().sorted(Comparator.comparing(Route::destinationCidrBlock)).collect(Collectors.toList());
//        verifyRoutes(routes_3.get(0), "0.0.0.0/0", "igw-031eafe9584def2c9");
//        verifyRoutes(routes_3.get(1), "10.0.0.0/16", "local");

        assertAll(
                () -> assertEquals(destination, route.destinationCidrBlock(),
                        "Expected destination CIDR block to be " + destination + " but was " + route.destinationCidrBlock()),
                () -> assertEquals(target, route.gatewayId(),
                        "Expected destination CIDR block to be " + target + " but was " + route.gatewayId())
        );
    }

    private void verifyRouteTables(RouteTable routeTable, String id, boolean main) {
        assertAll(
                () -> assertEquals(id, routeTable.routeTableId(),
                        "Expected routeTableId to be " + id + " but was " + routeTable.routeTableId()),
                () -> assertEquals(main, routeTable.associations().get(0).main(),
                        "Expected main to be " + main + " but was " + routeTable.associations().get(0).main())
        );
    }
}
