package kaiwens.basicapp;


import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClientBuilder;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingInstancesRequest;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClientBuilder;
import com.amazonaws.services.elasticloadbalancing.model.AmazonElasticLoadBalancingException;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerResult;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ELBv1Example {
    public static void main() {
        AmazonAutoScaling asg = AmazonAutoScalingClientBuilder.standard().withRegion("us-east-1").build();
        DescribeAutoScalingGroupsResult asgResult = asg.describeAutoScalingGroups(new DescribeAutoScalingGroupsRequest()
                .withAutoScalingGroupNames("kaiwenstestgroup"));
        List<Instance> instances = asgResult.getAutoScalingGroups().get(0).getInstances().stream().map(instance -> new Instance(instance.getInstanceId())).collect(Collectors.toList());
        System.out.println("input size is " + instances.size());
        AmazonElasticLoadBalancing elb = AmazonElasticLoadBalancingClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

//        RegisterInstancesWithLoadBalancerRequest request = new RegisterInstancesWithLoadBalancerRequest().withInstances(instances).withLoadBalancerName("clb-limit-test");
        DeregisterInstancesFromLoadBalancerRequest request = new DeregisterInstancesFromLoadBalancerRequest().withInstances(instances).withLoadBalancerName("clb-limit-test");
        try {
            System.out.println("start at " + Instant.now());
//            RegisterInstancesWithLoadBalancerResult result = elb.registerInstancesWithLoadBalancer(request);
            DeregisterInstancesFromLoadBalancerResult result = elb.deregisterInstancesFromLoadBalancer(request);
            IntStream.range(0, 1000).parallel().forEach(i -> {
                elb.deregisterInstancesFromLoadBalancer(request);
                System.out.println(i);
            });

            System.out.println("succeed");
            System.out.println(result);
            System.out.println(result.getInstances().size());
        } catch (AmazonElasticLoadBalancingException e) {
            System.out.println("failed");
            System.out.println(e.getClass().getName());
            System.out.println(e.getErrorMessage());
            System.out.println(e.getErrorCode());
            System.out.println(e.getMessage());
            System.out.println(e.getErrorType());
            System.out.println(e.getServiceName());
            System.out.println(e.getStatusCode());
            System.out.println(e.getCause() == null);
        } finally {
            asg.shutdown();
            elb.shutdown();
            System.out.println("end at " + Instant.now());
        }
    }
}
