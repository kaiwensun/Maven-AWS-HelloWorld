package kaiwens.basicapp;

import kaiwens.basicapp.kaiwens.basicapp.swf.GreeterMain;
import kaiwens.basicapp.kaiwens.basicapp.swf.GreeterWorker;

import java.util.Arrays;

public class SWFExample {

    public static void main() {
//        GreeterWorkflow greeter = new GreeterWorkflowImpl();
//        greeter.greet();
        boolean enableWorkflow = "true".equals(System.getProperty("workflow"));
        boolean enableActivity = "true".equals(System.getProperty("activity"));
        boolean enableStarter = "true".equals(System.getProperty("starter"));
        System.out.println(Arrays.asList(enableWorkflow, enableActivity, enableStarter));
        if (enableActivity ^ enableWorkflow) {
            try {
                if (enableWorkflow || enableActivity) {
                    GreeterWorker.main();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (enableStarter) {
            GreeterMain.main();
        }
    }
}
