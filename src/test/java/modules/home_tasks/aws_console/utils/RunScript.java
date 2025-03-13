package modules.home_tasks.aws_console.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class RunScript {

    public static String runConsoleCommand(String command) {
        StringBuilder output = new StringBuilder();
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (System.getProperty("os.name").contains("Windows")) {
            processBuilder.command("cmd.exe", "/c", command);
        } else {
            processBuilder.command("bash.exe", "-c", command);
        }
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("Success!\n" + output);
            } else {
                System.out.println("Failed to execute");
            }
        } catch (InterruptedException | IOException e) {
            System.out.println("Error during executing commands occurred:\n" + e.getMessage());
        }
        return output.toString();
    }
}
