package com.myapp;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;

public class DeploymentStack extends Stack {

    public DeploymentStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public DeploymentStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        final BackendService backendService = new BackendService(this, "BackendService");

        final ApiService apiService = new ApiService(this, "ApiService", backendService.queue);
    }
}
