package modules.home_tasks.aws_sdk.utils;

import io.qameta.allure.Step;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SubjectTerm;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static modules.home_tasks.aws_sdk.utils.MessageLogger.logMessage;


public class EmailHandler {

    @Step("Get email from gmail mailbox")
    public static Map<Message, Folder> getEmail(String emailID, String password, String subjectToBeSearched) throws Exception {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
//        props.setProperty("mail.smtp.starttls.required", "true");
//        props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", emailID, password);

        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);

        Message[] messages = null;
        boolean mailFound = false;
        Message email = null;

        for (int i = 0; i < 5; i++) {
            messages = folder.search(new SubjectTerm(subjectToBeSearched), folder.getMessages());
            if (messages == null || messages.length == 0) {
                Thread.sleep(1000);
            } else if (Stream.of(messages).noneMatch(message -> {
                try {
                    return !message.isSet(Flags.Flag.SEEN);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
                return false;
            })) {
                Thread.sleep(1000);
            }
        }

        for (Message mail : messages) {
            if (!mail.isSet(Flags.Flag.SEEN)) {
                email = mail;
                mailFound = true;
            }
        }
        if (!mailFound) {
            throw new Exception("Could not found Email");
        }
        Map<Message, Folder> emailMessageAndFolder = new HashMap<>();
        emailMessageAndFolder.put(email, folder);
        return emailMessageAndFolder;
    }

    @Step("Get text from message")
    public static String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain") || message.isMimeType("text/html")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        logMessage(result);
        return result;
    }

    @Step("Get text from MimeMultipart")
    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart)  throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = count - 1; i >= 0; i--) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break;
            } else if (bodyPart.isMimeType("text/html")) {
                result = result + "\n" + bodyPart.getContent();
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result = result + getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
            }
        }
        logMessage(result);
        return result;
    }

    @Step("Delete email from mailbox")
    public static void deleteEmail(Map<Message, Folder> emailMessageAndFolder) {
        Message message = emailMessageAndFolder.keySet().iterator().next();
        if (message != null) {
            try {
                message.setFlag(Flags.Flag.DELETED, true);
                emailMessageAndFolder.get(message).close(true);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    @Step("Check email was not received")
    public static boolean checkEmailNotReceived(String emailID, String password, String subjectToBeSearched) throws Exception {
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.smtp.starttls.required", "true");
        props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", emailID, password);

        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);

        Message[] messages = null;
        boolean mailFound = false;

        for (int i = 0; i < 10; i++) {
            messages = folder.search(new SubjectTerm(subjectToBeSearched), folder.getMessages());
            if (messages.length == 0) {
                Thread.sleep(10000);
            }
        }

        for (Message mail : messages) {
            if (!mail.isSet(Flags.Flag.SEEN)) {
                mailFound = true;
                mail.setFlag(Flags.Flag.SEEN, true);
            }
        }
        if (!mailFound) {
            System.out.println("Email with subject '" + subjectToBeSearched + "' has not been found");
            return true;
        }
        return false;
    }
}
