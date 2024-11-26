package kaiwens.basicapp;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.SessionCredentialsProviderFactory;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClientBuilder;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;

import java.time.Instant;
import java.util.Random;

public class StsExample {
    private static final String SESSION_NAME_PREFIX = "MyAwsJavaManualTest-";
    private static final Integer SESSION_DURATION = 900;


    public static void main() {
        String r = "us-northeast-1";
//        String acc ="325413137670";
        String acc = "359350711583";

        final String SERVICE_ROLE_ARN = String.format("arn:aws:iam::%s:role/instancegroup-role", acc);
        AWSCredentialsProvider credsProvider = new EnvironmentVariableCredentialsProvider();
        Region region = RegionUtils.getRegion(r);
        region.getAvailableEndpoints();
        EndpointConfiguration regionEndpointConfig = new EndpointConfiguration(region.getServiceEndpoint(AWSSecurityTokenService.ENDPOINT_PREFIX), region.getName());
        System.out.println(region.getServiceEndpoint(AWSSecurityTokenService.ENDPOINT_PREFIX));
        System.out.println(region.getName());
        System.out.println(region.getDomain());
        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
//                .withRegion(region.getName())
                .withEndpointConfiguration(regionEndpointConfig)
                .withCredentials(credsProvider)
                .build();
        String sessionName = SESSION_NAME_PREFIX + new Random().nextLong();
        System.out.println(String.format("Session name: %s", sessionName));
        AssumeRoleRequest request = new AssumeRoleRequest()
                .withDurationSeconds(SESSION_DURATION)
                .withRoleArn(SERVICE_ROLE_ARN)
                .withRoleSessionName(sessionName);
        request.putCustomRequestHeader("x-amz-source-account", acc);
        request.putCustomRequestHeader("x-amz-source-arn", String.format("arn:aws:iam::%s:role/instancegroup-role", acc));


        AmazonEC2 ec2Client = AmazonEC2ClientBuilder.standard()
                .withRegion(region.getName())
                .build();
        DescribeInstancesRequest ec2Request = new DescribeInstancesRequest();


        AmazonElasticLoadBalancing elbClient = AmazonElasticLoadBalancingClientBuilder.standard()
                .withRegion(region.getName())
                .build();
        DescribeLoadBalancersRequest elbRequest = new DescribeLoadBalancersRequest();

        try {
            System.out.println("start at " + Instant.now());
            Credentials creds = stsClient.assumeRole(request).getCredentials();

            AWSCredentialsProvider provider = new AWSCredentialsProvider() {
                @Override
                public AWSCredentials getCredentials() {return new AWSSessionCredentials() {
                        @Override
                        public String getAWSAccessKeyId() {
                            return creds.getAccessKeyId();
                        }

                        @Override
                        public String getAWSSecretKey() {
                            return creds.getSecretAccessKey();
                        }

                        @Override
                        public String getSessionToken() {
                            return creds.getSessionToken();
                        }
                    };
                }

                @Override
                public void refresh() {

                }
            };
            ec2Request.setRequestCredentialsProvider(provider);
            elbRequest.setRequestCredentialsProvider(provider);

            DescribeInstancesResult ec2Result = ec2Client.describeInstances(ec2Request);
            System.out.println(ec2Result);
            DescribeLoadBalancersResult elbResult =  elbClient.describeLoadBalancers(elbRequest);
            System.out.println(elbResult);
        } finally {
            stsClient.shutdown();
            ec2Client.shutdown();
            elbClient.shutdown();
            System.out.println("end at " + Instant.now());
        }
    }
}
