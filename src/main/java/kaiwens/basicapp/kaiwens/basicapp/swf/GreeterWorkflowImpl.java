package kaiwens.basicapp.kaiwens.basicapp.swf;

import com.amazonaws.services.simpleworkflow.flow.core.Promise;

public class GreeterWorkflowImpl implements GreeterWorkflow {

    private GreeterActivitiesClient operations = new GreeterActivitiesClientImpl();

    public void greet(String suffix) {
        Promise<String> name = operations.getName();
        Promise<String> greeting = operations.getGreeting(name + " My name is " + suffix);
        operations.say(greeting);
    }
}
