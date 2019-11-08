package kaiwens.basicapp.kaiwens.basicapp.swf;

import com.amazonaws.services.simpleworkflow.flow.core.Promise;

public class GreeterWorkflowImpl implements GreeterWorkflow {

    private GreeterActivitiesClient operations = new GreeterActivitiesClientImpl();

    public void greet(String suffix) {
        System.out.println("A: GreeterWorkflow calls getName()");
        Promise<String> name = operations.getName();
        System.out.println("name is ready? " + name.isReady());
        System.out.println("A: GreeterWorkflow calls getGreeting()");
        Promise<String> greeting = operations.getGreeting(name);
        System.out.println("greeting is ready? " + greeting.isReady());
        System.out.println("A: GreeterWorkflow calls say()");
        operations.say(greeting);
    }

    private void sleep() {
        try {
            System.out.println("sleeping");
            Thread.sleep(8 * 1000);
            System.out.println("i wake up");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
