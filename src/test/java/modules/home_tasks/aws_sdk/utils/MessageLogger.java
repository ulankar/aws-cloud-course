package modules.home_tasks.aws_sdk.utils;

import io.qameta.allure.Attachment;

public final class MessageLogger {

    private MessageLogger() {
        throw new IllegalStateException("Utility class is not meant to be instantiated");
    }

    @Attachment(value = "Attachment")
    public static String logMessage(String stream) {
        return stream;
    }
}
