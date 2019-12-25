package kaiwens.basicapp;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.AliasRoutingConfiguration;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.GetProvisionedConcurrencyConfigRequest;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.lambda.model.UpdateAliasRequest;

import java.util.HashMap;
import java.util.Map;

public class LambdaExample {
    public static void main() {
        AWSLambda lambda = AWSLambdaClientBuilder.standard().withRegion(Regions.SA_EAST_1).build();
        ListFunctionsResult result = lambda.listFunctions();
        System.out.println("Size: " + result.getFunctions().size());
        for (FunctionConfiguration config : result.getFunctions()) {
            System.out.println(config.getFunctionName());
        }
        try {
            lambda.getProvisionedConcurrencyConfig(
                    new GetProvisionedConcurrencyConfigRequest()
                            .withFunctionName("my-function-4")
                            .withQualifier("PROD"));
        } catch (com.amazonaws.services.lambda.model.AWSLambdaException e) {
            System.out.println(e.getStatusCode());
            System.out.println(e.getErrorCode());
        }

//        String funcName = "goodboy";
//        String alias = "PROD";
//        Map<String, Double> map = new HashMap<>();
//        map.put("2", 0.0);
//        AliasRoutingConfiguration routingConfig =
//                new AliasRoutingConfiguration().withAdditionalVersionWeights(map);
//        UpdateAliasRequest request =
//                new UpdateAliasRequest()
//                        .withFunctionName(funcName)
//                        .withFunctionVersion("1")
//                        .withName(alias)
//                        .withRoutingConfig(routingConfig);
//        lambda.updateAlias(request);
        lambda.shutdown();
    }
}
