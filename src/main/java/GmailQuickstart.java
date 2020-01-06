import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class GmailQuickstart extends Thread{
    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static String lastId = "0";
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

                if(messages.size() != lastSize){
                    Message lastMsg = messages.get(0);

                    Message msg = service.users().messages().get(user, lastMsg.getId()).execute();

                    String msgContent = getContent(msg);

                    if(msgContent.length() > 1024){
                        msgContent = msgContent.substring(0, 1024);
                    }

                    Main.jda.getGuildById(Main.SERVER_ID).getTextChannelById(Main.ANNOUNCEMENT_CHANNEL_ID).sendMessage(Main.getEmbed(
                            msg.getPayload().getHeaders().get(19).getValue(),
                            msg.getPayload().getHeaders().get(23).getValue(),
                            msg.getPayload().getHeaders().get(20).getValue(),
                            msgContent,
                            null
                    )).queue();
                    Main.jda.getGuildById(Main.SERVER_ID).getTextChannelById(Main.ANNOUNCEMENT_CHANNEL_ID).sendMessage("<@&" + Main.ANNOUNCEMENTS_ROLE_ID + "> Email announcement posted for 857").queue();

                    lastSize = messages.size();
                }

                Thread.sleep(1000);

            }


        } catch (Exception e){
            e.printStackTrace();
            System.out.println("welp");
        }
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