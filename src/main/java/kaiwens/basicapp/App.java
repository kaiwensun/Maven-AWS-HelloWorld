package kaiwens.basicapp;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.regions.Region;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.s3.model.Bucket;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
    private List<AmazonS3> clients;

    public App() {
        List<Regions> regions = getRegions();
        clients = createClients(regions);
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
        log.error("THIS ERROR MESSAGE IS PRINTED BY lombok and slf4j");
//        examplePassSystemPropertyFromPOM();
//        exampleLog4j2Level();


//        exampleS3Operations();


    }

    /*
     * Example static labs
     */
    private static void examplePassSystemPropertyFromPOM() {
        System.out.println(System.getProperty("abc"));
    }

    private static void exampleLog4j2Level() {
        Log log = LogFactory.getLog("com.amazonaws");
        log.debug("DEBUG!");
        log.info("INFO");
        log.warn("WARN");
        log.error("ERROR");
    }

    private static void exampleS3Operations() {
        App app = new App();
        app.createBucket();
        app.shutdownClients();
    }

    private static void exampleLambdaOperations() {
        AWSLambda lambda = AWSLambdaClientBuilder.defaultClient();
        ListFunctionsResult result = lambda.listFunctions();
        System.out.println("Size: " + result.getFunctions().size());
        for (FunctionConfiguration config : result.getFunctions()) {
            System.out.println(config.getFunctionName());
        }
        lambda.shutdown();
    }

    private static void exampleDynamoDB() {
        System.out.println(AmazonDynamoDB.ENDPOINT_PREFIX);
        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard();
        AmazonDynamoDB ddb = builder.build();
        Region.getRegion(Regions.US_WEST_2);
        ddb.shutdown();
    }

    /*
     * S3 helpers
     */

    private List<AmazonS3> createClients(List<Regions> regions) {
        return regions.stream().map(region -> AmazonS3ClientBuilder.standard().withRegion(region).build()).collect(Collectors.toList());
    }

    private List<Regions> getRegions() {
        return Arrays.asList(Regions.US_EAST_1, Regions.EU_WEST_1, Regions.AP_NORTHEAST_1);
    }

    private List<Bucket> createBucket() {
        return clients.stream().map(
                s3 -> {
                    String bucketName = "kaiwens-s3test-"+s3.getRegionName();
                    if (s3.doesBucketExistV2(bucketName)) {
                    }
                    return s3.createBucket(bucketName);
                }
        ).collect(Collectors.toList());
    }

    private void shutdownClients() {
        clients.forEach(client -> client.shutdown());
    }
}
