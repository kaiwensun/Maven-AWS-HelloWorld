package kaiwens.basicapp.kaiwens.basicapp.swf;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.amazonaws.services.simpleworkflow.model.DescribeDomainRequest;
import com.amazonaws.services.simpleworkflow.model.UnknownResourceException;
import com.amazonaws.services.worklink.model.DescribeDomainResult;

public class GreeterMain {
    public static void main() {
        ClientConfiguration config = new ClientConfiguration().withSocketTimeout(70 * 1000);
        AmazonSimpleWorkflow swf = AmazonSimpleWorkflowClient.builder().withClientConfiguration(config).build();
        String domain = "helloWorldWalkthrough";
        try {
            String res =
                    swf.describeDomain(new DescribeDomainRequest().withName(domain)).getDomainInfo().getName();
            System.out.println(res);
        } catch (UnknownResourceException e) {
            System.out.println(e);
        }
        GreeterWorkflowClientExternalFactory factory = new GreeterWorkflowClientExternalFactoryImpl(swf, domain);
        String executionId = "HappyWorld";
        GreeterWorkflowClientExternal greeter = factory.getClient(executionId);
        System.out.println("A: ClientExternal calls great");
        greeter.greet("Kevin");
        swf.shutdown();
    }
}
