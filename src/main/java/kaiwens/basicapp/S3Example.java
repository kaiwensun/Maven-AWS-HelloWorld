package kaiwens.basicapp;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class S3Example {
    private List<AmazonS3> clients;

    public S3Example() {
        List<Regions> regions = getRegions();
        clients = createClients(regions);
    }

    private static void exampleS3Operations() {
        S3Example app = new S3Example();
        app.createBucket();
        app.uploadObjects();
        app.getObjects();
        app.deleteBucket();
        app.shutdownClients();
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
