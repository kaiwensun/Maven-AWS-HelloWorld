package kaiwens.basicapp;

import com.amazonaws.services.codedeploy.AmazonCodeDeploy;
import com.amazonaws.services.codedeploy.AmazonCodeDeployClientBuilder;
import com.amazonaws.services.codedeploy.model.ApplicationDoesNotExistException;
import com.amazonaws.services.codedeploy.model.ApplicationInfo;
import com.amazonaws.services.codedeploy.model.ComputePlatform;
import com.amazonaws.services.codedeploy.model.CreateApplicationRequest;
import com.amazonaws.services.codedeploy.model.CreateDeploymentGroupRequest;
import com.amazonaws.services.codedeploy.model.DeploymentGroupDoesNotExistException;
import com.amazonaws.services.codedeploy.model.GetApplicationRequest;
import com.amazonaws.services.codedeploy.model.GetDeploymentGroupRequest;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AssociateRouteTableRequest;
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
import com.amazonaws.services.ecs.model.AssignPublicIp;
import com.amazonaws.services.ecs.model.AwsVpcConfiguration;
import com.amazonaws.services.ecs.model.Cluster;
import com.amazonaws.services.ecs.model.Compatibility;
import com.amazonaws.services.ecs.model.ContainerDefinition;
import com.amazonaws.services.ecs.model.CreateClusterRequest;
import com.amazonaws.services.ecs.model.CreateServiceRequest;
import com.amazonaws.services.ecs.model.DeploymentController;
import com.amazonaws.services.ecs.model.DeploymentControllerType;
import com.amazonaws.services.ecs.model.DescribeClustersRequest;
import com.amazonaws.services.ecs.model.DescribeServicesRequest;
import com.amazonaws.services.ecs.model.LaunchType;
import com.amazonaws.services.ecs.model.ListTaskDefinitionsRequest;
import com.amazonaws.services.ecs.model.NetworkConfiguration;
import com.amazonaws.services.ecs.model.NetworkMode;
import com.amazonaws.services.ecs.model.PortMapping;
import com.amazonaws.services.ecs.model.RegisterTaskDefinitionRequest;
import com.amazonaws.services.ecs.model.SchedulingStrategy;
import com.amazonaws.services.ecs.model.Service;
import com.amazonaws.services.ecs.model.TransportProtocol;
import com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancingClientBuilder;
import com.amazonaws.services.elasticloadbalancingv2.model.Action;
import com.amazonaws.services.elasticloadbalancingv2.model.ActionTypeEnum;
import com.amazonaws.services.elasticloadbalancingv2.model.CreateListenerRequest;
import com.amazonaws.services.elasticloadbalancingv2.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancingv2.model.CreateTargetGroupRequest;
import com.amazonaws.services.elasticloadbalancingv2.model.DescribeListenersRequest;
import com.amazonaws.services.elasticloadbalancingv2.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancingv2.model.DescribeTargetGroupsRequest;
import com.amazonaws.services.elasticloadbalancingv2.model.ForwardActionConfig;
import com.amazonaws.services.elasticloadbalancingv2.model.IpAddressType;
import com.amazonaws.services.elasticloadbalancingv2.model.Listener;
import com.amazonaws.services.elasticloadbalancingv2.model.LoadBalancer;
import com.amazonaws.services.elasticloadbalancingv2.model.LoadBalancerNotFoundException;
import com.amazonaws.services.elasticloadbalancingv2.model.LoadBalancerSchemeEnum;
import com.amazonaws.services.elasticloadbalancingv2.model.LoadBalancerStateEnum;
import com.amazonaws.services.elasticloadbalancingv2.model.LoadBalancerTypeEnum;
import com.amazonaws.services.elasticloadbalancingv2.model.ProtocolEnum;
import com.amazonaws.services.elasticloadbalancingv2.model.TargetGroup;
import com.amazonaws.services.elasticloadbalancingv2.model.TargetGroupNotFoundException;
import com.amazonaws.services.elasticloadbalancingv2.model.TargetGroupTuple;
import com.amazonaws.services.elasticloadbalancingv2.model.TargetTypeEnum;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.AttachRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.AttachedPolicy;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.GetRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.ListAttachedRolePoliciesRequest;
import com.amazonaws.services.identitymanagement.model.ListRolePoliciesRequest;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.Role;
import kaiwens.basicapp.kaiwens.basicapp.swf.Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CodeDeployECS {

    private final AmazonCodeDeploy codedeploy;
    private final AmazonEC2 ec2;
    private final AmazonECS ecs;
    private final AmazonElasticLoadBalancing elb;
    private final AmazonIdentityManagement iam;

    private final static List<String> AZs = Arrays.asList("us-west-2a", "us-west-2b");
    private final static String ALB_NAME = "ecs-dep-alb";
    private final static String BLUE_TG_NAME = "blue-tg";
    private final static String GREEN_TG_NAME = "green-tg";

    private final static String APP_NAME = "ecs-application";
    private final static String DEPLOYMENT_GROUP_GROUP = "ecs-dep-group";
    private final static String CD_SERVICE_ROLE_NAME = "CodeDeployServiceRoleForECS";
    private final static String CD_SERVICE_POLICY_ARN = "arn:aws:iam::aws:policy/AWSCodeDeployRoleForECS";
    private final static String CD_SERVICE_ROLE_TRUSTED_IDENTITY = "codedeploy.amazonaws.com";

    private final static String ECS_CLUSTER_NAME = "ecs-cluster";
    private final static String ECS_TASKEXECUTION_ROLE_NAME = "ecsTaskExecutionRole";
    private final static String ECS_TASKEXECUTION_POLICY_ARN =
            "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy";
    private final static String ECS_TASKEXECUTION_ROLE_TRUSTED_IDENTITY = "ecs-tasks.amazonaws.com";
    private final static String ECS_CONTAINER_NAME = "container-stored-in-ecr";

    private final static String TASK_DEFINITION_FAMILY_NAME = "manager-websites";
    private final static String ECS_SERVICE_NAME = "ecs-service";

    private LoadBalancer lb;
    private List<Subnet> subnets;
    private SecurityGroup sg;
    private TargetGroup blueTg;
    private TargetGroup greenTg;


    public static void main() {
        CodeDeployECS example = new CodeDeployECS();
        try {
            example.work();
        } finally {
            example.deconstruct();
        }
    }

    private void work() {
        // https://docs.aws.amazon.com/codedeploy/latest/userguide/tutorial-ecs-prereqs.html
        setupNetworkResources();
        setupEcsResources();
        prepareForDeployment();
    }

    private void setupEcsResources() {
        // Fargate for now
        // https://docs.aws.amazon.com/AmazonECS/latest/developerguide/create-blue-green.html step 2, 3 and 4
        Cluster cluster = getOrCreateEcsCluster();
        List<String> taskDefinitionArns = getOrCreateTaskDefinitions();
        getOrCreateEcsService(taskDefinitionArns.get(0), this.lb, this.sg, this.subnets, this.blueTg);

    }

    private Service getOrCreateEcsService(final String taskDefinitionArn, LoadBalancer lb, SecurityGroup sg,
                                          List<Subnet> subnets, TargetGroup blueTg) {
        List<Service> services = ecs.describeServices(new DescribeServicesRequest()
                .withCluster(ECS_CLUSTER_NAME)
                .withServices(ECS_SERVICE_NAME)
        ).getServices();
        assert (services.size() <= 1);
        Service service;
        if (services.size() == 0) {
            log.info("Creating ECS service");
            service = ecs.createService(new CreateServiceRequest()
                    .withServiceName(ECS_SERVICE_NAME)
                    .withCluster(ECS_CLUSTER_NAME)
                    .withTaskDefinition(taskDefinitionArn)
                    .withLoadBalancers(new com.amazonaws.services.ecs.model.LoadBalancer()
                            // either lb name or tg arn can be specified at the same time
                            // .withLoadBalancerName(lb.getLoadBalancerName())
                            .withContainerName(ECS_CONTAINER_NAME)
                            .withContainerPort(80)
                            .withTargetGroupArn(blueTg.getTargetGroupArn())
                    )
                    .withLaunchType(LaunchType.FARGATE)
                    .withSchedulingStrategy(SchedulingStrategy.REPLICA)
                    .withDeploymentController(new DeploymentController()
                            .withType(DeploymentControllerType.CODE_DEPLOY))
                    .withPlatformVersion("LATEST")
                    .withNetworkConfiguration(new NetworkConfiguration()
                            .withAwsvpcConfiguration(new AwsVpcConfiguration()
                                    .withAssignPublicIp(AssignPublicIp.ENABLED)
                                    .withSecurityGroups(sg.getGroupId())
                                    .withSubnets(subnets.stream().map(Subnet::getSubnetId).collect(Collectors.toList()))))
                    .withDesiredCount(3)
            ).getService();
        } else {
            service = services.get(0);
        }
        printResource("service", service);
        return service;
    }

    private Cluster getOrCreateEcsCluster() {
        List<Cluster> clusters = ecs.describeClusters(new DescribeClustersRequest()
                .withClusters(ECS_CLUSTER_NAME)
        ).getClusters();
        Cluster cluster;
        assert (clusters.size() <= 1);
        if (clusters.size() == 0) {
            cluster = ecs.createCluster(new CreateClusterRequest()
                    .withClusterName(ECS_CLUSTER_NAME)
            ).getCluster();
        } else {
            cluster = clusters.get(0);
        }
        return cluster;
    }

    private List<String> getOrCreateTaskDefinitions() {
        Role role = getOrCreateServiceRole(ECS_TASKEXECUTION_ROLE_NAME, ECS_TASKEXECUTION_POLICY_ARN, ECS_TASKEXECUTION_ROLE_TRUSTED_IDENTITY);
        List<String> taskDefinitionArns = ecs.listTaskDefinitions(new ListTaskDefinitionsRequest()
                .withFamilyPrefix(TASK_DEFINITION_FAMILY_NAME)
        ).getTaskDefinitionArns();
        if (taskDefinitionArns.size() != 0) {
            for (int revision : new int[]{1, 2}) {
                String taskDefinitionRevision = TASK_DEFINITION_FAMILY_NAME + ":" + revision;
                if (!taskDefinitionArns.stream().anyMatch(arn -> arn.endsWith("/" + taskDefinitionRevision))) {
                    throw new AssertionError("Can't find task definition " + taskDefinitionRevision +
                            ". Please manually create such task definition, or change code to use another task definition" +
                            " family.");
                }
            }
        } else {
            log.info("creating new task definitions");
            for (int revision : new int[]{1, 2}) {
                ecs.registerTaskDefinition(new RegisterTaskDefinitionRequest()
                        .withFamily(TASK_DEFINITION_FAMILY_NAME)
                        .withNetworkMode(NetworkMode.Awsvpc)
                        .withContainerDefinitions(Arrays.asList(80, 8080).stream().map(port ->
                                new ContainerDefinition()
                                        .withName(ECS_CONTAINER_NAME)
                                        .withImage("httpd:2.4")
                                        .withEssential(true)
                                        .withEntryPoint("sh", "-c")
                                        .withCommand(getEcsTaskCommand("v" + revision))
                                        .withPortMappings(new PortMapping()
                                                .withContainerPort(port)
                                                .withHostPort(port)
                                                .withProtocol(TransportProtocol.Tcp)))
                                .collect(Collectors.toList()))
                        .withRequiresCompatibilities(Compatibility.FARGATE)
                        .withCpu("256")
                        .withMemory("512")
                        .withExecutionRoleArn(role.getArn())
                );
            }
            taskDefinitionArns = ecs.listTaskDefinitions(new ListTaskDefinitionsRequest()
                    .withFamilyPrefix(TASK_DEFINITION_FAMILY_NAME)).getTaskDefinitionArns();
        }
        assert (taskDefinitionArns.size() == 2);
        return taskDefinitionArns;
    }

    private String getEcsTaskCommand(String version) {
        return "\"/bin/sh -c \\\"echo 'Your application is now running on a container in Amazon ECS (" + version +
                ")' > /usr/local/apache2/htdocs/index.html && httpd-foreground\\\"";
    }

    private Role getOrCreateServiceRole(final String rollName, final String serviceRoleArn,
                                        final String serviceRoleAssumer) {
        Role role;
        try {
            role = iam.getRole(new GetRoleRequest().withRoleName(rollName)).getRole();
        } catch (NoSuchEntityException e) {
            log.info("Creating role " + rollName);
            role = iam.createRole(new CreateRoleRequest()
                    .withRoleName(rollName)
                    .withAssumeRolePolicyDocument(getServiceRoleAssuleRolePolicyDocument(serviceRoleAssumer))
            ).getRole();
            Utils.eventually(() -> {
                try {
                    iam.getRole(new GetRoleRequest().withRoleName(rollName)).getRole();
                    return null;
                } catch (NoSuchEntityException e2) {
                    throw new AssertionError(e2);
                }
            }, 100, 10);
        }
        makesurePolicyAttachedToRole(role, serviceRoleArn);
        return role;
    }

    private void makesurePolicyAttachedToRole(Role role, final String managedPolicyArn) {
        List<AttachedPolicy> attachedPolicies = iam.listAttachedRolePolicies(new ListAttachedRolePoliciesRequest()
                .withRoleName(role.getRoleName())
        ).getAttachedPolicies().stream().filter(
                policy -> policy.getPolicyArn().equals(managedPolicyArn)
        ).collect(Collectors.toList());
        assert (attachedPolicies.size() <= 1);
        if (attachedPolicies.size() == 0) {
            log.info("Attaching " + managedPolicyArn + " to role " + role.getRoleName());
            iam.attachRolePolicy(new AttachRolePolicyRequest()
                    .withRoleName(role.getRoleName())
                    .withPolicyArn(managedPolicyArn)
            );
        }
    }

    private void prepareForDeployment() {
        // https://docs.aws.amazon.com/codedeploy/latest/userguide/getting-started-codedeploy.html
        Role role = getOrCreateServiceRole(
                CD_SERVICE_ROLE_NAME, CD_SERVICE_POLICY_ARN, CD_SERVICE_ROLE_TRUSTED_IDENTITY);

        // https://docs.aws.amazon.com/codedeploy/latest/userguide/applications-create-cli.html

//        createApplication();
    }
    /*

    private String createApplication() {
        String applicationId;
        try {
            applicationId = codedeploy.getApplication(new GetApplicationRequest()
                    .withApplicationName(APP_NAME)
            ).getApplication().getApplicationId();
        } catch (ApplicationDoesNotExistException e) {
            log.info("Creating new CodeDeploy application " + APP_NAME);
            applicationId = codedeploy.createApplication(new CreateApplicationRequest()
                    .withApplicationName(APP_NAME)
                    .withComputePlatform(ComputePlatform.ECS)
            ).getApplicationId();
        }
        return applicationId;
    }

    private String createDeploymentGroup() {
        String deploymentGroupId;
        try {
            deploymentGroupId = codedeploy.getDeploymentGroup(new GetDeploymentGroupRequest()
                    .withApplicationName(APP_NAME)
                    .withDeploymentGroupName(DEPLOYMENT_GROUP_GROUP)
            ).getDeploymentGroupInfo().getDeploymentGroupId();
        } catch (DeploymentGroupDoesNotExistException e) {
            deploymentGroupId = codedeploy.createDeploymentGroup(new CreateDeploymentGroupRequest()
                    .withApplicationName(APP_NAME)
                    .withDeploymentGroupName(DEPLOYMENT_GROUP_GROUP)

            ).getDeploymentGroupId();
        }
    }

     */

    private void setupNetworkResources() {
        // https://docs.aws.amazon.com/codedeploy/latest/userguide/deployment-groups-create-load-balancer-for-ecs.html

        // Verify Your Default VPC, Public Subnets, and Security Group
        Vpc vpc = getDefaultVpc();
        List<Subnet> subnets = getSubnets(vpc.getVpcId());
        SecurityGroup sg = getDefaultSecurityGroup(vpc);

        // Ceate an Amazon EC2 Application Load Balancer, Two Target Groups, and Listeners
        LoadBalancer lb = getOrCreateLoadBalancer(subnets, sg);
        TargetGroup blueTg = getOrCreateTargetGroup(BLUE_TG_NAME, vpc.getVpcId());
        TargetGroup greenTg = getOrCreateTargetGroup(GREEN_TG_NAME, vpc.getVpcId());
        getOrCreateListener(lb, blueTg, 80);
        getOrCreateListener(lb, greenTg, 8080);

        this.subnets = subnets;
        this.sg = sg;
        this.blueTg = blueTg;
        this.greenTg = greenTg;
    }

    private Listener getOrCreateListener(LoadBalancer lb, TargetGroup tg, Integer port) {
        List<Listener> listeners = elb.describeListeners(new DescribeListenersRequest()
                .withLoadBalancerArn(lb.getLoadBalancerArn())).getListeners();
        listeners = listeners.stream().filter(listener -> listener.getPort().equals(port)).collect(Collectors.toList());
        if (listeners.size() == 0) {
            log.info("Creating listener for target group " + tg.getTargetGroupName() + " at port " + port);
            listeners = elb.createListener(new CreateListenerRequest()
                    .withLoadBalancerArn(lb.getLoadBalancerArn())
                    .withProtocol(ProtocolEnum.HTTP)
                    .withPort(port)
                    .withDefaultActions(new Action()
                            .withType(ActionTypeEnum.Forward)
                            .withForwardConfig(new ForwardActionConfig()
                                    .withTargetGroups(new TargetGroupTuple()
                                            .withTargetGroupArn(tg.getTargetGroupArn())
                                            .withWeight(1)
                                    )
                            )
                    )
            ).getListeners();
        }
        assert (listeners.size() == 1);
        Listener listener = listeners.get(0);
        assert (listener.getDefaultActions().size() == 1);
        assert (listener.getDefaultActions().get(0).getForwardConfig().getTargetGroups().size() == 1);
        assert (listener.getDefaultActions().get(0).getForwardConfig().getTargetGroups().get(0).getTargetGroupArn().equals(tg.getTargetGroupArn()));
        assert (listener.getProtocol().equals(ProtocolEnum.HTTP.toString()));
        assert (listener.getPort().equals(port));
        return listener;
    }

    private TargetGroup getOrCreateTargetGroup(final String tgName, final String vpcId) {
        List<TargetGroup> tgs;
        try {
            tgs = elb.describeTargetGroups(new DescribeTargetGroupsRequest().withNames(tgName)).getTargetGroups();
        } catch (TargetGroupNotFoundException e) {
            log.info("Creating target group " + tgName);
            tgs = elb.createTargetGroup(new CreateTargetGroupRequest()
                    .withName(tgName)
                    .withProtocol(ProtocolEnum.HTTP)
                    .withPort(80)
                    .withTargetType(TargetTypeEnum.Ip)
                    .withVpcId(vpcId)
            ).getTargetGroups();
        }
        assert (tgs.size() == 1);
        return tgs.get(0);
    }

    private LoadBalancer getOrCreateLoadBalancer(List<Subnet> subnets, SecurityGroup sg) {
        List<LoadBalancer> lbs;
        try {
            lbs = elb.describeLoadBalancers(new DescribeLoadBalancersRequest().withNames(ALB_NAME)).getLoadBalancers();
            assert (lbs.size() == 1);
        } catch (LoadBalancerNotFoundException e) {
            log.info("Creating new load balancer");
            lbs = elb.createLoadBalancer(new CreateLoadBalancerRequest()
                    .withName(ALB_NAME)
                    .withSubnets(subnets.stream().map(Subnet::getSubnetId).collect(Collectors.toList()))
                    .withType(LoadBalancerTypeEnum.Application) // this is default
                    .withSecurityGroups(sg.getGroupId())
                    .withScheme(LoadBalancerSchemeEnum.InternetFacing)
                    .withIpAddressType(IpAddressType.Ipv4)
            ).getLoadBalancers();
        }
        LoadBalancer lb = lbs.get(0);
        assert (lb.getType().equals(LoadBalancerTypeEnum.Application.toString()));
        Utils.eventually(
                () -> {
                    assert (lb.getState().getCode().equals(LoadBalancerStateEnum.Active.toString()));
                    return null;
                },
                null,
                null
        );
        this.lb = lb;
        return lb;
    }

    private CodeDeployECS() {
        codedeploy = AmazonCodeDeployClientBuilder.defaultClient();
        ec2 = AmazonEC2ClientBuilder.defaultClient();
        ecs = AmazonECSClientBuilder.defaultClient();
        elb = AmazonElasticLoadBalancingClientBuilder.defaultClient();
        iam = AmazonIdentityManagementClientBuilder.defaultClient();
    }

    private void deconstruct() {
        codedeploy.shutdown();
        ec2.shutdown();
        ecs.shutdown();
        elb.shutdown();
        iam.shutdown();
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
                    makeSureSubnetPublic(subnet, routeTable);
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

    private void makeSureSubnetPublic(Subnet subnet, RouteTable routeTable) {
        int association = (int) (routeTable.getAssociations().stream().filter(
                routeTableAssociation -> subnet.getSubnetId().equals(routeTableAssociation.getSubnetId())
        ).count());
        assert (association <= 1);
        if (association == 0) {
            log.info("associating route table");
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
        List<Vpc> vpcs = ec2.describeVpcs(new DescribeVpcsRequest()
                .withFilters(new Filter().withName("isDefault").withValues("true"))
        ).getVpcs();
        assert (vpcs.size() == 1);
        return vpcs.get(0);
    }

    private String getServiceRoleAssuleRolePolicyDocument(final String serviceRoleAssumer) {
        return "{\n" +
                "    \"Version\": \"2012-10-17\",\n" +
                "    \"Statement\": [\n" +
                "        {\n" +
                "            \"Sid\": \"\",\n" +
                "            \"Effect\": \"Allow\",\n" +
                "            \"Principal\": {\n" +
                "                \"Service\": [\n" +
                "                    \"" + serviceRoleAssumer + "\"\n" +
                "                ]\n" +
                "            },\n" +
                "            \"Action\": \"sts:AssumeRole\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
    }

}
