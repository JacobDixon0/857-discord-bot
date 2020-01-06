import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.model.*;
import org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

import java.io.*;
import java.lang.Thread;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GmailQuickstart extends Thread{
    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static int lastSize = 0;

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GmailQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    @Override
    public void run() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Print the labels in the user's account.
            String user = "me";

            while (true){
                ListMessagesResponse listMessagesResponse = service.users().messages().list(user).execute();
                List<Message> messages = listMessagesResponse.getMessages();

                if(messages.size() != lastSize && lastSize != 0){
                    Message lastMsg = messages.get(0);

                    Message msg = service.users().messages().get(user, lastMsg.getId()).execute();

                    String msgContent = getContent(msg);

                    String from = headerValueGetterThing("from", msg.getPayload().getHeaders());
                    String to = headerValueGetterThing("to", msg.getPayload().getHeaders());
                    String date = headerValueGetterThing("date", msg.getPayload().getHeaders());
                    String sub = headerValueGetterThing("subject", msg.getPayload().getHeaders());

                    if(to.equals("first857-l@mtu.edu") ||
                            from.equals("Sloth King <sloth@royalslothking.com>") ||
                            from.equals("Jacob Dixon <jd@jacobdixon.us>")){
                        Main.jda.getGuildById(Main.SERVER_ID).getTextChannelById(Main.ANNOUNCEMENT_CHANNEL_ID).sendMessage(Main.getEmbed(
                                from, sub, date, msgContent, getAttachments(service, user, msg.getId()))).queue();

                        Main.jda.getGuildById(Main.SERVER_ID).getTextChannelById(Main.ANNOUNCEMENT_CHANNEL_ID)
                                .sendMessage("<@&" + Main.ANNOUNCEMENTS_ROLE_ID + "> Email announcement posted for 857")
                                .queue();
                    }

                    lastSize = messages.size();
                }

                Thread.sleep(1000);

            }

        } catch (Exception e){
            e.printStackTrace();
            System.out.println("welp");
        }
    }

    private static String headerValueGetterThing(String name, List<MessagePartHeader> headers){

        String result = "";

        for(MessagePartHeader mph : headers){
            if(mph.getName().toLowerCase().equals(name.toLowerCase())){
                result = mph.getValue();
                break;
            }
        }

        return result;
    }

    /*public static void run(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Print the labels in the user's account.
        String user = "me";

        ListMessagesResponse listMessagesResponse = service.users().messages().list(user).execute();
        List<Message> messages = listMessagesResponse.getMessages();

        *//*ListLabelsResponse listResponse = service.users().labels().list(user).execute();
        List<Label> labels = listResponse.getLabels();
        if (labels.isEmpty()) {
            System.out.println("No labels found.");
        } else {
            System.out.println("Labels:");
            for (Label label : labels) {
                System.out.printf("- %s\n", label.getName());
            }
        }*//*
    }*/

    public static List<String> getAttachments(Gmail service, String userId, String messageId)
            throws IOException {

        List<String> result = new ArrayList<>();

        Message message = service.users().messages().get(userId, messageId).execute();
        List<MessagePart> parts = message.getPayload().getParts();
        for (MessagePart part : parts) {
            if (part.getFilename() != null && part.getFilename().length() > 0) {
                String filename = part.getFilename();
                String attId = part.getBody().getAttachmentId();
                if(attId == null) return null;
                MessagePartBody attachPart = service.users().messages().attachments().
                        get(userId, messageId, attId).execute();

                Base64 base64Url = new Base64(true);
                byte[] fileByteArray = base64Url.decodeBase64(attachPart.getData());
                FileOutputStream fileOutFile =
                        new FileOutputStream("/var/www/html/bot-things/attachments-cache/" + filename);
                result.add("https://www.jacobdixon.us/bot-things/attachments-cache/" + filename);
                fileOutFile.write(fileByteArray);
                fileOutFile.close();
            }
        }

        return result;
    }

    public static String getContent(Message message) {
        StringBuilder stringBuilder = new StringBuilder();

            getPlainTextFromMessageParts(message.getPayload().getParts(), stringBuilder);
            byte[] bodyBytes = Base64.decodeBase64(stringBuilder.toString());
            String text = new String(bodyBytes, StandardCharsets.UTF_8);
            return text;

    }

    private static void getPlainTextFromMessageParts(List<MessagePart> messageParts, StringBuilder stringBuilder) {
        for (MessagePart messagePart : messageParts) {
            if (messagePart.getMimeType().equals("text/plain")) {
                stringBuilder.append(messagePart.getBody().getData());
            }

            if (messagePart.getParts() != null) {
                getPlainTextFromMessageParts(messagePart.getParts(), stringBuilder);
            }
        }
    }

}