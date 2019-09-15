package kaiwens.basicapp;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

public class SNSExample {
    public static void exampleSNS() {
        AmazonSNS client =
                AmazonSNSClientBuilder.standard().withRegion("us-west-2").withCredentials(new ProfileCredentialsProvider("admin")).build();
        String topicArn = "arn:aws:sns:us-west-2:513730896679:pingpong-tube";
        String message = "Hello Chime!";
        PublishResult result = client.publish(new PublishRequest(topicArn, message));
        System.out.println(result);
        client.shutdown();
    }
}
