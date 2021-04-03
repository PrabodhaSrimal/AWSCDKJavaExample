package com.myapp;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ApiHandler implements RequestHandler<APIGatewayV2HTTPEvent, String> {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String sqsQueueUrl = System.getenv("SQS_QUEUE_URL");
    private static final AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();

    public ApiHandler() {
    }

    @Override
    public String handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        final LambdaLogger logger = context.getLogger();
        String response = new String();

        // log execution details
        logger.log("ENVIRONMENT VARIABLES: " + gson.toJson(System.getenv()));
        logger.log("CONTEXT: " + gson.toJson(context));
        logger.log("EVENT: " + gson.toJson(event));

        // process event
        logger.log("Parameters = " + event.getPathParameters().toString());
        logger.log(event.getBody());

        SendMessageRequest sendMsg = new SendMessageRequest()
            .withQueueUrl(sqsQueueUrl)
            .withMessageBody(gson.toJson(event));
        sqsClient.sendMessage(sendMsg);

        // process Lambda API response
        response = "success";

        return response;
    }
}

