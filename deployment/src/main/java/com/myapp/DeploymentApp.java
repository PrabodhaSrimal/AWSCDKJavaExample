package com.myapp;

import software.amazon.awscdk.core.App;

public class DeploymentApp {
    public static void main(final String[] args) {
        App app = new App();

        new DeploymentStack(app, "UnicornApp");

        app.synth();
    }
}
