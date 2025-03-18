package modules.home_tasks.aws_sdk.tests.module_4;

import io.qameta.allure.Step;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.SsmException;

import static modules.home_tasks.aws_sdk.tests.module_4.Ec2DescriptionUtils.getEc2KeyName;
import static modules.home_tasks.aws_sdk.utils.FileUtils.writePemFile;

public class SsmConnector {
    private final SsmClient ssmClient;

    public SsmConnector() {
        this.ssmClient = SsmClient.builder()
                .build();
    }

    @Step("Get secure parameter")
    public String getSecureParameter(String parameterName) {
        try {
            // Create request with decryption flag
            GetParameterRequest parameterRequest = GetParameterRequest.builder()
                    .name(parameterName)
                    .withDecryption(true)  // Important for SecureString parameters
                    .build();

            GetParameterResponse parameterResponse = ssmClient.getParameter(parameterRequest);

            return parameterResponse.parameter().value();

        } catch (SsmException e) {
            System.err.println("Error retrieving parameter: " + e.getMessage());
            throw e;
        }
    }

    public void closeClient() {
        if (ssmClient != null) {
            ssmClient.close();
        }
    }

    @Step("Get access key")
    public static void getAccessKey() {
        SsmConnector ssmConnector = new SsmConnector();
        String secureParameter = ssmConnector.getSecureParameter("/ec2/keypair/" + getEc2KeyName());
//        System.out.println("Secure parameter value:\n" + secureParameter);
        ssmConnector.closeClient();
        writePemFile(secureParameter);
    }
}
