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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        System.out.println( "Hello World!" );
        examplePassSystemPropertyFromPOM();
        exampleLog4j2Level();
//        System.out.println(AmazonDynamoDB.ENDPOINT_PREFIX);
//        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard();
//        AmazonDynamoDB ddb = builder.build();
//        Region.getRegion(Regions.US_WEST_2);
//        ddb.shutdown();

        AWSLambda lambda = AWSLambdaClientBuilder.defaultClient();
        ListFunctionsResult result = lambda.listFunctions();
        System.out.println("Size: " + result.getFunctions().size());
        for (FunctionConfiguration config : result.getFunctions()) {
            System.out.println(config.getFunctionName());
        }
        lambda.shutdown();
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
}
