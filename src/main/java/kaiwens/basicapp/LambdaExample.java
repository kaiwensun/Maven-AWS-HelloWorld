package kaiwens.basicapp;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListFunctionsResult;

public class LambdaExample {
    private static void exampleLambdaOperations() {
        AWSLambda lambda = AWSLambdaClientBuilder.defaultClient();
        ListFunctionsResult result = lambda.listFunctions();
        System.out.println("Size: " + result.getFunctions().size());
        for (FunctionConfiguration config : result.getFunctions()) {
            System.out.println(config.getFunctionName());
        }
        lambda.shutdown();
    }
}
