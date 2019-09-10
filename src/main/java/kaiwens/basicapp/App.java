package kaiwens.basicapp;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.codedeploy.model.AppSpecContent;
import com.amazonaws.services.codedeploy.model.CreateDeploymentRequest;
import com.amazonaws.services.codedeploy.AmazonCodeDeploy;
import com.amazonaws.services.codedeploy.AmazonCodeDeployClientBuilder;
import com.amazonaws.services.codedeploy.model.RawString;
import com.amazonaws.services.codedeploy.model.RevisionLocation;
import com.amazonaws.services.codedeploy.model.RevisionLocationType;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.regions.Region;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.s3.model.ObjectListing;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class App {
    private List<AmazonS3> clients;

    public App() {
//        List<Regions> regions = getRegions();
//        clients = createClients(regions);
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
        exampleCodeDeploy();

    }

    /*
     * Example labs entry points
     */

    private static void exampleCodeDeploy() {
        AmazonCodeDeploy codeDeploy = AmazonCodeDeployClientBuilder.standard().build();

        RevisionLocation revisionLocation = new RevisionLocation()
                .withRevisionType(RevisionLocationType.String)
                .withString(new RawString().withContent(
//                .withAppSpecContent(new AppSpecContent().withContent(
                    "{\n" +
                    " \t\"version\": 0.0,\n" +
                    " \t\"Resources\": [{\n" +
                    " \t\t\"myLambdaFunction\": {\n" +
                    " \t\t\t\"Type\": \"AWS::Lambda::Function\",\n" +
                    " \t\t\t\"Properties\": {\n" +
                    " \t\t\t\t\"Name\": \"my-function\",\n" +
                    " \t\t\t\t\"Alias\": \"PROD\",\n" +
                    " \t\t\t\t\"CurrentVersion\": \"1\",\n" +
                    " \t\t\t\t\"TargetVersion\": \"2\"\n" +
                    " \t\t\t}\n" +
                    " \t\t}\n" +
                    " \t}],\n" +
                    " \t\"Hooks\": [{\n" +
                    " \t\t\t\"BeforeAllowTraffic\": \"LambdaFunctionToValidateBeforeTrafficShift\"\n" +
                    "      },\n" +
                    "      {\n" +
                    " \t\t\t\"AfterAllowTraffic\": \"LambdaFunctionToValidateAfterTrafficShift\"\n" +
                    " \t\t}\n" +
                    " \t]\n" +
                    " }"));

        CreateDeploymentRequest request = new CreateDeploymentRequest()
                .withApplicationName("my-lambda")
                .withDeploymentGroupName("my-group")
                .withRevision(revisionLocation);

        codeDeploy.createDeployment(request);
        codeDeploy.shutdown();

    }
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
        app.uploadObjects();
        app.getObjects();
        app.deleteBucket();
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
        return regions.stream().map(region -> AmazonS3ClientBuilder.standard()
                // .withRegion(region)
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration("s3." + region.getName() + ".amazonaws.com",
                                region.getName()))
                .build()
        ).collect(Collectors.toList());
    }

    private List<Regions> getRegions() {
        return Arrays.asList(Regions.US_EAST_1, Regions.EU_WEST_1, Regions.AP_NORTHEAST_1);
    }

    private void createBucket() {
        clients.forEach(
                s3 -> {
                    String bucketName = getBucketName(s3);
                    if (s3.doesBucketExistV2(bucketName)) {
                        log.warn("The bucket " + bucketName + " is already created.");
                    } else {
                        s3.createBucket(bucketName);
                    }
                }
        );
    }

    private void deleteBucket() {
        clients.forEach(s3 -> {
            val bucketName = getBucketName(s3);
            boolean first = true;
            for (ObjectListing objectListing = s3.listObjects(bucketName);
                 objectListing.isTruncated() || first;
                 objectListing = s3.listNextBatchOfObjects(objectListing)) {
                first = false;
                objectListing.getObjectSummaries().forEach(
                        s3ObjectSummary -> s3.deleteObject(bucketName, s3ObjectSummary.getKey())
                );
            }
            s3.deleteBucket(bucketName);
        });
    }

    private String getBucketName(AmazonS3 client) {
        return "kaiwens-s3test-" + client.getRegionName();
    }

    private void uploadObjects() {
        val localPath = "/Users/kaiwens/workplace/test/";
        clients.forEach(
                s3 -> {
                    val bucketName = getBucketName(s3);
                    val objectName = "out.txt";
                    val file = new File(localPath + objectName);
                    if (s3.doesObjectExist(bucketName, objectName)) {
                        log.warn("Object exists: " + bucketName + "/" + objectName);
                    } else {
                        s3.putObject(bucketName, objectName, file);
                    }
                }
        );
    }

    private void getObjects() {
        clients.forEach(s3 -> {
            val bucketName = getBucketName(s3);
            val objectName = "out.txt";
            String objectString = s3.getObjectAsString(bucketName, objectName);
            System.out.println("Got from " + bucketName + ": " + objectString);
        });
    }

    private void shutdownClients() {
        clients.forEach(client -> client.shutdown());
    }

}
