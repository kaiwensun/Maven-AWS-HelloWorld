package kaiwens.basicapp.kaiwens.basicapp.swf;

public class GreeterActivitiesImpl implements GreeterActivities {
    @Override
    public String getName() {
        return "SWF's World";
    }

    @Override
    public String getGreeting(String name) {
        return "Hello " + name + "!";
    }

    @Override
    public void say(String what) {
        System.out.println(what);
    }
}