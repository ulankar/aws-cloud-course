package modules.home_tasks.aws_sdk.tests.module_4;

import com.jcraft.jsch.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import io.qameta.allure.Step;
import software.amazon.awssdk.services.ec2.model.Instance;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static modules.home_tasks.aws_sdk.tests.module_4.Ec2DescriptionUtils.getEc2Instances;
import static modules.home_tasks.aws_sdk.tests.module_4.Ec2DescriptionUtils.getPrivateEc2Instance;
import static modules.home_tasks.aws_sdk.tests.module_4.Ec2DescriptionUtils.getPublicEc2Instance;
import static modules.home_tasks.aws_sdk.tests.module_4.SsmConnector.getAccessKey;
import static modules.home_tasks.aws_sdk.utils.FileUtils.getPathToPemFile;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Connect with SSH tests")
public class ConnectWithSshTest {

    private static final List<Instance> instances = getEc2Instances();
    private static String publicHost; // Private EC2 (only accessible via Public EC2)
    private static String privateHost; // Public EC2
    private static String keyPath; // Path to SSH Private Key
    private final String user = "ec2-user"; // Default for Amazon Linux
    private final int localPort = 2222; // Port for SSH tunnel

    @BeforeAll
    public static void setUp() {
        getAccessKey(); // Get the SSH Private Key
        publicHost = getPublicEc2Instance(instances).publicIpAddress();
        privateHost = getPrivateEc2Instance(instances).privateIpAddress();
        keyPath = getPathToPemFile();
        System.out.println("Public Host: " + publicHost + ",\nPrivate Host: " + privateHost);
    }

    @Test
    @DisplayName("Connect to Private EC2 instance via Public EC2 instance")
    public void testConnectionToPrivateInstance() {
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(keyPath);

            // Connect to Public Host (Public EC2)
            Session publicSession = jsch.getSession(user, publicHost, 22);
            publicSession.setConfig("StrictHostKeyChecking", "no");
            publicSession.connect();
            System.out.println("Connected to Public Host: " + publicHost);

            // Create an SSH Tunnel to Private EC2
            int assignedPort = publicSession.setPortForwardingL(localPort, privateHost, 22);
            System.out.println("SSH Tunnel Created: localhost:" + assignedPort + " -> " + privateHost + ":22");

            // Connect to Private EC2 through the Tunnel
            Session privateSession = jsch.getSession(user, "localhost", assignedPort);
            privateSession.setConfig("StrictHostKeyChecking", "no");
            privateSession.connect();
            System.out.println("Connected to Private EC2: " + privateHost);

            // Run a simple command (e.g., 'hostname')
            ChannelExec channel = (ChannelExec) privateSession.openChannel("exec");
            channel.setCommand("curl localhost");
            channel.setInputStream(null);
            channel.setErrStream(System.err);
            channel.connect();
            String result = readExecutionResult(channel);
            assertAll(
                    () -> assertTrue(result.contains("\"availability_zone\": \"eu-central-1a\""),
                            "Expected \"availability_zone\": \"eu-central-1a\" in the output"),
                    () -> assertTrue(result.contains("\"private_ipv4\": \"" + privateHost + "\""),
                            "Expected \"private_ipv4\": \"" + privateHost + "\" in the output"),
                    () -> assertTrue(result.contains("\"region\": \"eu-central-1\""),
                            "Expected \"region\": \"eu-central-1\" in the output"));

            channel.disconnect();
            privateSession.disconnect();
            publicSession.disconnect();
            System.out.println("Disconnected from all sessions.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Connect to the Internet from Private EC2 instance via Public EC2 instance")
    public void testConnectionToInternetFromPrivateInstance() {
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(keyPath);

            // Connect to Public Host (Public EC2)
            Session publicSession = jsch.getSession(user, publicHost, 22);
            publicSession.setConfig("StrictHostKeyChecking", "no");
            publicSession.connect();
            System.out.println("Connected to Public Host: " + publicHost);

            // Create an SSH Tunnel to Private EC2
            int assignedPort = publicSession.setPortForwardingL(localPort, privateHost, 22);
            System.out.println("SSH Tunnel Created: localhost:" + assignedPort + " -> " + privateHost + ":22");

            // Connect to Private EC2 through the Tunnel
            Session privateSession = jsch.getSession(user, "localhost", assignedPort);
            privateSession.setConfig("StrictHostKeyChecking", "no");
            privateSession.connect();
            System.out.println("Connected to Private EC2: " + privateHost);

            ChannelExec channel = (ChannelExec) privateSession.openChannel("exec");
            System.out.println("Run command to access Internet from Private EC2");

            channel.setCommand("curl https://www.google.com/");
            channel.setInputStream(null);
            channel.setErrStream(System.err);
            channel.connect();

            String result = readExecutionResult(channel);
            assertAll(
                    () -> assertTrue(result.contains("GoogleDoodle")),
                    () -> assertTrue(result.contains("http://schema.org/WebPage")));
            channel.disconnect();

            privateSession.disconnect();
            publicSession.disconnect();
            System.out.println("Disconnected from all sessions.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Connect to Public EC2 instance")
    public void testConnectionToPublicInstance() {
        String publicInstancePrivateIp = getPublicEc2Instance(instances).privateIpAddress();
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(keyPath);

            // Connect to Public Host (Public EC2)
            Session publicSession = jsch.getSession(user, publicHost, 22);
            publicSession.setConfig("StrictHostKeyChecking", "no");
            publicSession.connect();
            System.out.println("Connected to Public Host: " + publicHost);

            ChannelExec channel = (ChannelExec) publicSession.openChannel("exec");
            channel.setCommand("curl localhost");
            channel.setInputStream(null);
            channel.setErrStream(System.err);
            channel.connect();
            String result = readExecutionResult(channel);
            assertAll(
                    () -> assertTrue(result.contains("\"availability_zone\": \"eu-central-1a\""),
                            "Expected \"availability_zone\": \"eu-central-1a\" in the output"),
                    () -> assertTrue(result.contains("\"private_ipv4\": \"" + publicInstancePrivateIp + "\""),
                            "Expected \"private_ipv4\": \"" + publicInstancePrivateIp + "\" in the output"),
                    () -> assertTrue(result.contains("\"region\": \"eu-central-1\""),
                            "Expected \"region\": \"eu-central-1\" in the output"));

            channel.disconnect();
            publicSession.disconnect();
            System.out.println("Disconnected from all sessions.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Connect to Public EC2 instance with port 80 fails")
    public void testConnectionToPublicInstanceWithPort80Fails() {
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(keyPath);
            Session publicSession = jsch.getSession(user, publicHost, 80);
            publicSession.setConfig("StrictHostKeyChecking", "no");
            assertThrows(JSchException.class, publicSession::connect);

            publicSession.disconnect();
            System.out.println("Disconnected from all sessions.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("Connect to Internet from Public EC2 instance")
    public void testConnectionToInternetFromPublicInstance() {
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(keyPath);

            // Connect to Public Host (Public EC2)
            Session publicSession = jsch.getSession(user, publicHost, 22);
            publicSession.setConfig("StrictHostKeyChecking", "no");
            publicSession.connect();
            System.out.println("Connected to Public Host: " + publicHost);

            ChannelExec channel = (ChannelExec) publicSession.openChannel("exec");
            channel.setCommand("curl https://www.google.com/");
            channel.setInputStream(null);
            channel.setErrStream(System.err);
            channel.connect();
            String result = readExecutionResult(channel);
            assertAll(
                    () -> assertTrue(result.contains("GoogleDoodle")),
                    () -> assertTrue(result.contains("http://schema.org/WebPage")));

            channel.disconnect();
            publicSession.disconnect();
            System.out.println("Disconnected from all sessions.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Step("Read the command output")
    private static String readExecutionResult(ChannelExec channel) throws IOException, InterruptedException {
        // Read the command output
        String result = "";
        InputStream input = channel.getInputStream();
        byte[] tmp = new byte[1024];
        while (true) {
            while (input.available() > 0) {
                int i = input.read(tmp, 0, 1024);
                if (i < 0) break;
                result += new String(tmp, 0, i);
                System.out.print(new String(tmp, 0, i));
            }
            if (channel.isClosed()) break;
            Thread.sleep(1000);
        }
        return result;
    }
}
