package modules.home_tasks.aws_sdk.utils;

import modules.home_tasks.BasicHooks;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import static modules.home_tasks.PropertyHandler.setPropertiesForEnvironment;
import static modules.home_tasks.aws_sdk.tests.module_4.SsmConnector.getAccessKey;
import static modules.home_tasks.aws_sdk.tests.module_6.utils.CloudFormationOutputReader.getSpecificOutput;
import static modules.home_tasks.aws_sdk.tests.module_6.utils.SecretsManagerReader.getDatabaseCredentials;
import static modules.home_tasks.aws_sdk.tests.module_6.utils.SecretsManagerReader.getJdbcUrl;


public class AwsHooks extends BasicHooks {

    public static RdsClient rdsClient;
    public static CloudFormationClient cfnClient;
    public static SecretsManagerClient secretsClient;
    public static SqsClient sqsClient;
    public static SnsClient snsClient;

    public static final Region region = Region.EU_CENTRAL_1;
    public static final String stackName = "cloudximage";
    public static final String ec2_username = "ec2-user";
    public static final String db_username = "mysql_admin";
    public static final String db_name = "cloudximages";
    public static final String queueNamePrefix = "cloudximage-QueueSQSQueue";
    public static final String emailAddress = "turnir.iproaction.test@gmail.com";

    public static String bucket_name;
    public static String db_instance_identifier;
    public static String db_instance_arn;
    public static String db_host;
    public static String db_port;
    public static String key_id;
    public static String db_secret_name;
    public static String public_ip;
    public static String db_password;
    public static String jdbc_url;
    public static String public_dns;
    public static String topicArn;

    @BeforeAll
    public static void setUpClient() {
        cfnClient = CloudFormationClient.builder()
                .region(region)
                .build();
        rdsClient = RdsClient.builder()
                .region(region)
                .build();
        secretsClient = SecretsManagerClient.builder()
                .region(region)
                .build();
        sqsClient = SqsClient.builder()
                .region(region)
                .build();
        snsClient = SnsClient.builder()
                .region(region)
                .build();
        setUpParameters();
        setPropertiesForEnvironment();
        getAccessKey(); // Get the SSH Private Key
    }

    private static void setUpParameters() {
        bucket_name = getSpecificOutput(stackName, "ImageBucketName");
        db_host = getSpecificOutput(stackName, "DatabaseHost");
        public_dns = getSpecificOutput(stackName, "AppInstancePublicDns");
        db_port = getSpecificOutput(stackName, "DatabasePort");
        key_id = getSpecificOutput(stackName, "KeyId");
        public_ip = getSpecificOutput(stackName, "AppInstancePublicIp");
        db_instance_arn = getSpecificOutput(stackName, "DatabaseInstanceArn");
        jdbc_url = getJdbcUrl(db_host, db_port, db_name);
        db_instance_identifier = db_instance_arn
                .replace("arn:aws:rds:eu-central-1:124355674251:db:", "");
        db_secret_name = getSpecificOutput(stackName, "DatabaseSecretName");
        db_password = getDatabaseCredentials(db_secret_name).getPassword();
        topicArn = getSpecificOutput(stackName, "TopicArn");

        System.out.println("Bucket name: " + bucket_name + "\n" +
                "DB host: " + db_host + "\n" +
                "DB port: " + db_port + "\n" +
                "Key ID: " + key_id + "\n" +
                "DB secret name: " + db_secret_name + "\n" +
                "Public IP: " + public_ip + "\n" +
                "DB instance ARN: " + db_instance_arn + "\n" +
                "DB instance identifier: " + db_instance_identifier + "\n" +
                "JDBC URL: " + jdbc_url + "\n" +
                "DB password: " + db_password + "\n" +
                "Public DNS: " + public_dns
        );
    }

    @AfterAll
    public static void tearDown() {
        rdsClient.close();
        sqsClient.close();
        snsClient.close();
        cfnClient.close();
        secretsClient.close();
        System.out.println("\n============TEST CLASS EXECUTION FINISHED============");
    }
}
