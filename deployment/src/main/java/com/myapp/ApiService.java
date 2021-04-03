package com.myapp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import software.amazon.awscdk.core.CfnOutput;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.apigatewayv2.HttpMethod;
import software.amazon.awscdk.services.apigatewayv2.integrations.LambdaProxyIntegration;
import software.amazon.awscdk.services.sqs.Queue;

public class ApiService extends Construct {

    @SuppressWarnings("serial")
    public ApiService(final Construct scope, final String id, Queue backendQueue) {
        super(scope, id);

        final Function apiHandler = Function.Builder.create(this, "api-handler")
            .description("API gateway endpoint handler")
            .code(Code.fromAsset("../api/target/api-0.1.jar"))
            .handler("com.myapp.ApiHandler::handleRequest")
            .runtime(Runtime.JAVA_8)
            .timeout(Duration.seconds(10))
            .environment(new HashMap<String, String>() {
                {
                    put("SQS_QUEUE_URL", backendQueue.getQueueUrl());
                }
            })
            .build();

        backendQueue.grantSendMessages(apiHandler);

        // BuildRestApi(apiHandler); // old way
        BuildHttpApi(apiHandler); // new way
    }

    void BuildRestApi(Function apiHandler) {
        final RestApi api = RestApi.Builder.create(this, "api")
            .restApiName("My REST API")
            .description("Super duper API to serve unicorns")
            .retainDeployments(false)
            .build();

        CfnOutput.Builder.create(this, "api-url")
            .description("REST API URL")
            .exportName("api-url")
            .value(api.getUrl())
            .build();

        Map<String, String> lambdaIntegrationMap = new HashMap<String, String>();
        lambdaIntegrationMap.put("application/json", "{ \"statusCode\": \"200\" }");

        LambdaIntegration getEndpointIntegration = LambdaIntegration.Builder
            .create(apiHandler)
            .requestTemplates(lambdaIntegrationMap)
            .build();

        api.getRoot().addMethod("GET", getEndpointIntegration);

        LambdaIntegration postEndpointIntegration = new LambdaIntegration(apiHandler);
        LambdaIntegration deleteEndpointIntegration = new LambdaIntegration(apiHandler);

        Resource resource = api.getRoot().addResource("/unicorns/{id}");

        resource.addMethod("GET", getEndpointIntegration);
        resource.addMethod("POST", postEndpointIntegration);
        resource.addMethod("DELETE", deleteEndpointIntegration);
    }

    void BuildHttpApi(Function apiHandler) {
        final HttpApi api = HttpApi.Builder.create(this, "api")
            .apiName("My HTTP API")
            .description("Super duper API to serve unicorns")
            .build();

        CfnOutput.Builder.create(this, "api-url")
            .description("REST API URL")
            .exportName("api-url")
            .value(api.getUrl())
            .build();

        final LambdaProxyIntegration defaultIntegration = LambdaProxyIntegration.Builder.create()
            .handler(apiHandler)
            .build();

        api.addRoutes(AddRoutesOptions.builder()
            .path("/unicorns/{id}")
            .methods(Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.DELETE))
            .integration(defaultIntegration)
            .build());
    }
}
