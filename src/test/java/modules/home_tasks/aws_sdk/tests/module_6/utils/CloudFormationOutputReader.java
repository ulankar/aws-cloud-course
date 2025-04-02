package modules.home_tasks.aws_sdk.tests.module_6.utils;

import modules.home_tasks.aws_sdk.utils.AwsHooks;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.Output;
import software.amazon.awssdk.services.cloudformation.model.Stack;
import software.amazon.awssdk.services.cloudformation.model.CloudFormationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudFormationOutputReader extends AwsHooks {

    public static Map<String, String> getStackOutputs(String stackName) {
        Map<String, String> outputMap = new HashMap<>();

        try {
            DescribeStacksRequest request = DescribeStacksRequest.builder()
                    .stackName(stackName)
                    .build();

            DescribeStacksResponse response = cfnClient.describeStacks(request);
            List<Stack> stacks = response.stacks();

            if (stacks.isEmpty()) {
                throw new RuntimeException("Stack not found: " + stackName);
            }

            Stack stack = stacks.get(0);
            List<Output> outputs = stack.outputs();

            for (Output output : outputs) {
                outputMap.put(output.outputKey(), output.outputValue());

                if (output.description() != null) {
                    outputMap.put(output.outputKey() + "_Description", output.description());
                }
            }

            return outputMap;

        } catch (CloudFormationException e) {
            System.err.println("Error reading stack outputs: " + e.getMessage());
            throw e;
        }
    }

    public static String getSpecificOutput(String stackName, String outputKey) {
        try {
            Map<String, String> outputs = getStackOutputs(stackName);
            String outputValue = outputs.get(outputKey);

            if (outputValue == null) {
                throw new RuntimeException(
                        String.format("Output key '%s' not found in stack '%s'", outputKey, stackName)
                );
            }

            return outputValue;

        } catch (CloudFormationException e) {
            System.err.println("Error reading specific output: " + e.getMessage());
            throw e;
        }
    }

    public static void printStackOutputs(String stackName) {
        try {
            Map<String, String> outputs = getStackOutputs(stackName);

            System.out.println("\n=== Stack Outputs for " + stackName + " ===");
            outputs.forEach((key, value) -> {
                if (!key.endsWith("_Description")) {
                    System.out.println("Key: " + key);
                    System.out.println("Value: " + value);

                    String description = outputs.get(key + "_Description");
                    if (description != null) {
                        System.out.println("Description: " + description);
                    }
                    System.out.println();
                }
            });

        } catch (CloudFormationException e) {
            System.err.println("Error printing stack outputs: " + e.getMessage());
            throw e;
        }
    }
}
