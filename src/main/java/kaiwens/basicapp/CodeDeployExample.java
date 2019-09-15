package kaiwens.basicapp;

import com.amazonaws.services.codedeploy.AmazonCodeDeploy;
import com.amazonaws.services.codedeploy.AmazonCodeDeployClientBuilder;
import com.amazonaws.services.codedeploy.model.CreateDeploymentRequest;
import com.amazonaws.services.codedeploy.model.RawString;
import com.amazonaws.services.codedeploy.model.RevisionLocation;
import com.amazonaws.services.codedeploy.model.RevisionLocationType;

public class CodeDeployExample {
    public static void exampleCodeDeploy() {
        AmazonCodeDeploy codeDeploy = AmazonCodeDeployClientBuilder.standard().build();

        RevisionLocation revisionLocation = new RevisionLocation()
                .withRevisionType(RevisionLocationType.String)
                .withString(new RawString().withContent(
//                .withAppSpecContent(new AppSpecContent().withContent(
                        "{\n" +
                                " \t\"version\": 0.0,\n" +
                                " \t\"Resources\": [{\n" +
                                " \t\t\"myLambdaFunction\": {\n" +
                                " \t\t\t\"Type\": \"AWS::Lambda::Function\",\n" +
                                " \t\t\t\"Properties\": {\n" +
                                " \t\t\t\t\"Name\": \"my-function\",\n" +
                                " \t\t\t\t\"Alias\": \"PROD\",\n" +
                                " \t\t\t\t\"CurrentVersion\": \"1\",\n" +
                                " \t\t\t\t\"TargetVersion\": \"2\"\n" +
                                " \t\t\t}\n" +
                                " \t\t}\n" +
                                " \t}],\n" +
                                " \t\"Hooks\": [{\n" +
                                " \t\t\t\"BeforeAllowTraffic\": \"LambdaFunctionToValidateBeforeTrafficShift\"\n" +
                                "      },\n" +
                                "      {\n" +
                                " \t\t\t\"AfterAllowTraffic\": \"LambdaFunctionToValidateAfterTrafficShift\"\n" +
                                " \t\t}\n" +
                                " \t]\n" +
                                " }"));

        CreateDeploymentRequest request = new CreateDeploymentRequest()
                .withApplicationName("my-lambda")
                .withDeploymentGroupName("my-group")
                .withRevision(revisionLocation);

        codeDeploy.createDeployment(request);
        codeDeploy.shutdown();

    }
}
