package com.myapp;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BackendHandler implements RequestHandler<SQSEvent, String> {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String bucketName = System.getenv("BUCKET_NAME");
    private static final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

    public BackendHandler() {
    }

    @Override
    public String handleRequest(SQSEvent event, Context context) {
        final LambdaLogger logger = context.getLogger();

        String response = new String();

        // log execution details
        logger.log("ENVIRONMENT VARIABLES: " + gson.toJson(System.getenv()));
        logger.log("CONTEXT: " + gson.toJson(context));
        logger.log("EVENT: " + gson.toJson(event));

        // process event
        for (SQSMessage msg : event.getRecords()) {
            logger.log(msg.getBody());
        }


        final String destKey =  "/backend/" + context.getAwsRequestId();
        logger.log("Writing to: " + bucketName + destKey);
        try {
            s3Client.putObject(bucketName, destKey, gson.toJson(event));
        } catch (AmazonServiceException e) {
            logger.log(e.getErrorMessage());
            System.exit(1);
        }

        // process Lambda API response
        response = "success";

        return response;
    }
}

