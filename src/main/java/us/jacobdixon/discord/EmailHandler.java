/*
 * Name: 857-discord-bot
 * Date: 2020/1/11
 * Author(s): jd@jacobdixon.us (Jacob Dixon)
 * Version: 1.0a
 */

package us.jacobdixon.discord;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailHandler extends Thread {
    private static final String APPLICATION_NAME = "857 Discord Bot Tools";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static String lastId = "0";
    private static boolean running = false;

    private static int retryCount = 0;

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = EmailHandler.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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
        runInboxPolling();
    }

    private static void runInboxPolling(){
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            String user = "me";

            running = true;
            while (running) {

                ListMessagesResponse listMessagesResponse = service.users().messages().list(user).execute();
                List<Message> messages = listMessagesResponse.getMessages();

                if (!messages.get(0).getId().equals(lastId) && !lastId.equals("0")) {

                    Message lastMsg = messages.get(0);
                    Message msg = service.users().messages().get(user, lastMsg.getId()).execute();

                    String msgContent = getContent(msg);

                    String from = headerValueGetterThing("from", msg.getPayload().getHeaders());
                    String to = headerValueGetterThing("to", msg.getPayload().getHeaders());
                    String date = headerValueGetterThing("date", msg.getPayload().getHeaders());
                    String sub = headerValueGetterThing("subject", msg.getPayload().getHeaders());

                    SimpleDateFormat messageDateFormat = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss Z");
                    SimpleDateFormat gmailDateFormat = new SimpleDateFormat("MMM d, yyyy, h:m a");

                    String formattedDate = date;

                    Main.log("Received email from: \"" + from + "\".");

                    try {
                        formattedDate = gmailDateFormat.format(messageDateFormat.parse(date));
                    } catch (Exception e) {
                        Main.log(Main.LogPriority.ERROR, "Exception caught while attempting to parse email date.");
                    }

                    boolean allowed = false;

                    EmailSenderProfile emailSenderProfile = new EmailSenderProfile("name", "address", null);

                    Matcher nameMatcher = Pattern.compile("^(.+)<(.+)>$").matcher(from);

                    if (nameMatcher.find() && nameMatcher.group(1) != null && nameMatcher.group(2) != null) {
                        emailSenderProfile.setSenderName(nameMatcher.group(1).trim());
                        emailSenderProfile.setSenderAddress(nameMatcher.group(2).trim());
                    }

                    for (String dest : Main.knownDestinations) {
                        if (to.contains(dest)) {
                            allowed = true;
                            break;
                        }
                    }

                    if (!allowed) {
                        for (EmailSenderProfile sender : Main.knownSenders) {
                            if ((sender.getSenderName() + " <" + sender.getSenderAddress().toLowerCase() + ">").equals(from)) {
                                allowed = true;
                                emailSenderProfile = sender;
                                break;
                            }
                        }
                    }

                    if (allowed) {
                        String content = formatMessage(msgContent);
                        List<String> attachments = getAttachments(service, user, msg.getId());

                        Main.emailAnnounce(emailSenderProfile, sub, formattedDate, content, attachments);
                        Main.log("Announced email from: \"" + emailSenderProfile.getSenderAddress() + "\" with subject: \"" + sub + "\".");
                    }
                }

                lastId = messages.get(0).getId();
                Thread.sleep(4000);
                retryCount = 0;
            }
        } catch (Exception e) {
            Main.log(e);
            Main.log(Main.LogPriority.ERROR, "Encountered error while handling Gmail API.");
            if(++retryCount <= 4){
                Main.log("Reattempting to run Gmail API handler.");
                runInboxPolling();
            }
        }
    }

    private static String headerValueGetterThing(String name, List<MessagePartHeader> headers) {
        String result = "";

        for (MessagePartHeader mph : headers) {
            if (mph.getName().toLowerCase().equals(name.toLowerCase())) {
                result = mph.getValue();
                break;
            }
        }

        return result;
    }

    public static List<String> getAttachments(Gmail service, String userId, String messageId)
            throws IOException {

        List<String> result = new ArrayList<>();

        Message message = service.users().messages().get(userId, messageId).execute();
        List<MessagePart> parts = message.getPayload().getParts();
        for (MessagePart part : parts) {
            if (part.getFilename() != null && part.getFilename().length() > 0) {
                String filename = part.getFilename();
                String attId = part.getBody().getAttachmentId();
                if (attId == null) return null;
                MessagePartBody attachPart = service.users().messages().attachments().
                        get(userId, messageId, attId).execute();

                Base64 base64Url = new Base64(true);
                byte[] fileByteArray = base64Url.decodeBase64(attachPart.getData());
                if (new File(Main.cacheLocation).exists()) {
                    String timestamp = new SimpleDateFormat("yyyy.MM.dd.").format(new Date());
                    FileOutputStream fileOutFile = new FileOutputStream(Main.cacheLocation + timestamp + filename);
                    result.add(formatUrl("https://" + Main.domain + Main.extCacheLocation + timestamp + filename));
                    fileOutFile.write(fileByteArray);
                    fileOutFile.close();
                    Main.log("Created email attachment cache file: \"" + Main.cacheLocation + timestamp + filename + "\".");
                } else {
                    result.add("ERROR: Failed to load " + filename);
                }
            }
        }
        return result;
    }

    public static String formatUrl(String s) {
        return s.replaceAll(" ", "%20");
    }

    public static String formatMessage(String s) {
        for (String filter : Main.emailFilters) {
            s = Pattern.compile(filter, Pattern.DOTALL).matcher(s).replaceAll("");
        }
        return s;
    }

    public static String getContent(Message message) {
        StringBuilder stringBuilder = new StringBuilder();

        getPlainTextFromMessageParts(message.getPayload().getParts(), stringBuilder);
        byte[] bodyBytes = Base64.decodeBase64(stringBuilder.toString());
        return new String(bodyBytes, StandardCharsets.UTF_8);
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