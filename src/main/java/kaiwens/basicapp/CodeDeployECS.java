package kaiwens.basicapp;

import com.amazonaws.services.codedeploy.AmazonCodeDeploy;
import com.amazonaws.services.codedeploy.AmazonCodeDeployClientBuilder;
import com.amazonaws.services.codedeploy.model.CreateDeploymentRequest;
import com.amazonaws.services.codedeploy.model.RawString;
import com.amazonaws.services.codedeploy.model.RevisionLocation;
import com.amazonaws.services.codedeploy.model.RevisionLocationType;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AssociateRouteTableRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeInternetGatewaysRequest;
import com.amazonaws.services.ec2.model.DescribeRouteTablesRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSubnetsRequest;
import com.amazonaws.services.ec2.model.DescribeVpcsRequest;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.InternetGateway;
import com.amazonaws.services.ec2.model.RouteTable;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Vpc;
import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.AmazonECSClientBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CodeDeployECS {

//    https://docs.aws.amazon.com/codedeploy/latest/userguide/deployment-groups-create-load-balancer-for-ecs.html

    private final AmazonCodeDeploy codeDeploy;
    private final AmazonEC2 ec2;
    private final AmazonECS ecs;

    private final static List<String> AZs = Arrays.asList("us-west-2a", "us-west-2b");


    public static void main() {
        CodeDeployECS example = new CodeDeployECS();
        try {
            example.work();
        } finally {
            example.deconstruct();
        }
    }

    private void work() {
        Vpc vpc = getDefaultVpc();
        List<Subnet> subnets = getSubnets(vpc.getVpcId());

        printResource("vpc", vpc);

        SecurityGroup sg = getDefaultSecurityGroup(vpc);
        printResource("security group", sg);

        printResource("subnets", subnets);
    }

    private CodeDeployECS() {
        codeDeploy = AmazonCodeDeployClientBuilder.defaultClient();
        ec2 = AmazonEC2ClientBuilder.defaultClient();
        ecs = AmazonECSClientBuilder.defaultClient();
    }

    private void deconstruct() {
        codeDeploy.shutdown();
        ec2.shutdown();
        ecs.shutdown();
    }

    private SecurityGroup getDefaultSecurityGroup(Vpc vpc) {
        List<SecurityGroup> securityGroups =
                ec2.describeSecurityGroups(new DescribeSecurityGroupsRequest().withFilters(
                        new Filter().withName("group-name").withValues("default"),
                        new Filter().withName("vpc-id").withValues(vpc.getVpcId())))
                        .getSecurityGroups();
        assert (securityGroups.size() == 1);
        return securityGroups.get(0);
    }

    private List<Subnet> getSubnets(String vpcId) {
        RouteTable routeTable = getMainRouteTable(vpcId);
        return AZs.stream().map(
                az -> {
                    List<Subnet> subnets = ec2.describeSubnets(
                            new DescribeSubnetsRequest().withFilters(
                                    new Filter().withName("vpc-id").withValues(vpcId),
                                    new Filter().withName("default-for-az").withValues("true"),
                                    new Filter().withName("availability-zone").withValues(az))
                    ).getSubnets();
                    assert (subnets.size() == 1);
                    Subnet subnet = subnets.get(0);
                    makesureSubnetPublic(subnet, routeTable);
                    return subnet;
                }
        ).collect(Collectors.toList());
    }

    private RouteTable getMainRouteTable(final String vpcId) {
        List<RouteTable> routeTables = ec2.describeRouteTables(new DescribeRouteTablesRequest().withFilters(
                new Filter().withName("association.main").withValues("true"),
                new Filter().withName("vpc-id").withValues(vpcId)
        )).getRouteTables();
        assert (routeTables.size() == 1);
        RouteTable routeTable = routeTables.get(0);
        InternetGateway igw = getGatewayByVpc(vpcId);
        assert (routeTable.getRoutes().stream().filter(
                route -> route.getGatewayId().equals(igw.getInternetGatewayId()) && route.getDestinationCidrBlock().equals("0.0.0.0/0")
        ).count() == 1);
        return routeTable;
    }

    private void makesureSubnetPublic(Subnet subnet, RouteTable routeTable) {
        int association = (int) (routeTable.getAssociations().stream().filter(
                routeTableAssociation -> subnet.getSubnetId().equals(routeTableAssociation.getSubnetId())
        ).count());
        assert(association <= 1);
        if (association == 0) {
            ec2.associateRouteTable(new AssociateRouteTableRequest().withSubnetId(subnet.getSubnetId()).withRouteTableId(routeTable.getRouteTableId()));
        }
    }

    private InternetGateway getGatewayByVpc(final String vpcId) {
        List<InternetGateway> igws = ec2.describeInternetGateways(new DescribeInternetGatewaysRequest().withFilters(
                new Filter().withName("attachment.vpc-id").withValues(vpcId)
        )).getInternetGateways();
        assert (igws.size() == 1);
        return igws.get(0);
    }

    private static void printResource(String name, Object resource) {
        System.out.println("=================================================================");
        System.out.println(name);
        System.out.println(resource);
    }

    private Vpc getDefaultVpc() {
        List<Vpc> vpcs =
                ec2.describeVpcs(new DescribeVpcsRequest().withFilters(new Filter().withName("isDefault").withValues(
                        "true"))).getVpcs();
        assert (vpcs.size() == 1);
        return vpcs.get(0);
    }

}
