package kaiwens.basicapp.kaiwens.basicapp.swf;

public class GreeterActivitiesImpl implements GreeterActivities {
    @Override
    public String getName() {
        System.out.println("A: GreeterActivities getName() is called.");
        return "SWF's World";
    }

    @Override
    public String getGreeting(String name) {
        System.out.println("A: GreeterActivities getGreeting() is called.");
        return "Hello " + name + "!";
    }

    @Override
    public void say(String what) {
        System.out.println("A: GreeterActivities say() is called.");
        System.out.println(what);
    }
}