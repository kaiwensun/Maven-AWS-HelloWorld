package kaiwens.basicapp.kaiwens.basicapp.swf;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClientBuilder;
import com.amazonaws.services.simpleworkflow.flow.ActivityWorker;
import com.amazonaws.services.simpleworkflow.flow.WorkflowWorker;

public class GreeterWorker {
    public static void main() throws Exception {
        System.out.println(GreeterWorker.class);
        ClientConfiguration config = new ClientConfiguration().withSocketTimeout(70 * 1000);
        AmazonSimpleWorkflow service = AmazonSimpleWorkflowClientBuilder.standard().withClientConfiguration(config).build();
        String domain = "helloWorldWalkthrough";
        String taskListToPoll = "HellowWorldTaskList";

        ActivityWorker activityWorker = new ActivityWorker(service, domain, taskListToPoll);
        activityWorker.addActivitiesImplementation(new GreeterActivitiesImpl());
        System.out.println("Start running activity worker");
        activityWorker.start();

        WorkflowWorker workflowWorker = new WorkflowWorker(service, domain, taskListToPoll);
        workflowWorker.addWorkflowImplementationType(GreeterWorkflowImpl.class);
        System.out.println("Start running workflow worker");
        workflowWorker.start();
    }
}
