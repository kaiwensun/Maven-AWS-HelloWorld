package kaiwens.basicapp.kaiwens.basicapp.swf;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;

public class GreeterMain {
    public static void main() {
        ClientConfiguration config = new ClientConfiguration().withSocketTimeout(70 * 1000);
        AmazonSimpleWorkflow swf = AmazonSimpleWorkflowClient.builder().withClientConfiguration(config).build();
        String domain = "helloWorldWalkthrough";
        GreeterWorkflowClientExternalFactory factory = new GreeterWorkflowClientExternalFactoryImpl(swf, domain);
        GreeterWorkflowClientExternal greeter = factory.getClient("HappyWorld");
        greeter.greet("Kevin");
        swf.shutdown();
    }
}
