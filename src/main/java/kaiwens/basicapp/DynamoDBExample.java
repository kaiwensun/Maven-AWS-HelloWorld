package kaiwens.basicapp;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

public class DynamoDBExample {

    private static void exampleDynamoDB() {
        System.out.println(AmazonDynamoDB.ENDPOINT_PREFIX);
        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard();
        AmazonDynamoDB ddb = builder.build();
        Region.getRegion(Regions.US_WEST_2);
        ddb.shutdown();
    }

}
