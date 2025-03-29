package modules.home_tasks.aws_sdk.tests.module_6.utils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class RDSSecureConnector {
    private final String ec2Host;
    private final String rdsEndpoint;
    private final String dbName;
    private final String dbUsername;
    private final String dbPassword;
    private final String ec2Username;
    private final String pemFilePath;
    private Session sshSession;
    private static final int LOCAL_PORT = 3366; // Local port for tunneling
    private static final int RDS_PORT = 3306;   // Default MySQL port

    public RDSSecureConnector(String ec2Host,
                              String rdsEndpoint,
                              String dbName,
                              String dbUsername,
                              String dbPassword,
                              String ec2Username,
                              String pemFilePath) {
        this.ec2Host = ec2Host;
        this.rdsEndpoint = rdsEndpoint;
        this.dbName = dbName;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.ec2Username = ec2Username;
        this.pemFilePath = pemFilePath;
    }

    /**
     * Establishes SSH tunnel and returns database connection
     */
    public Connection connectToRDS() throws Exception {
        // Create SSH tunnel
        createSSHTunnel();

        // Wait for tunnel to be established
        Thread.sleep(1000);

        // Create database connection through SSH tunnel
        String jdbcUrl = String.format("jdbc:mysql://localhost:%d/%s", LOCAL_PORT, dbName);

        return DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
    }

    /**
     * Creates SSH tunnel to EC2 instance
     */
    private void createSSHTunnel() throws Exception {
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(pemFilePath);

            // Create SSH session to EC2
            sshSession = jsch.getSession(ec2Username, ec2Host, 22);

            // Skip host key verification (use with caution)
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(config);

            // Connect to EC2
            sshSession.connect();
            System.out.println("SSH session established to EC2");

            // Create port forwarding
            sshSession.setPortForwardingL(LOCAL_PORT, rdsEndpoint, RDS_PORT);
            System.out.println("Port forwarding established");

        } catch (Exception e) {
            throw new Exception("Failed to create SSH tunnel: " + e.getMessage(), e);
        }
    }

    /**
     * Closes the SSH tunnel
     */
    public void closeSSHTunnel() {
        if (sshSession != null && sshSession.isConnected()) {
            sshSession.disconnect();
        }
    }
}
