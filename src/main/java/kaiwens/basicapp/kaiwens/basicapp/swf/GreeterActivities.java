package kaiwens.basicapp.kaiwens.basicapp.swf;

import com.amazonaws.services.simpleworkflow.flow.annotations.Activities;
import com.amazonaws.services.simpleworkflow.flow.annotations.ActivityRegistrationOptions;

@ActivityRegistrationOptions(
        defaultTaskScheduleToStartTimeoutSeconds = 300,
        defaultTaskStartToCloseTimeoutSeconds = 1)
@Activities(version = "1.0")
public interface GreeterActivities {
    public String getName();

    public String getGreeting(String name);

    public void say(String what);
}