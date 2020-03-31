package us.jacobdixon.discord.email;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import us.jacobdixon.discord.AdvancedGuild;
import us.jacobdixon.discord.Database;
import us.jacobdixon.discord.Messages;
import us.jacobdixon.utils.Logger;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

public class EmailHandler extends Thread {
    private static final String APPLICATION_NAME = "857 Discord Bot";

    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private final List<String> SCOPES = Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM);

    private String tokensDirectoryPath;
    private String credentialsFilePath;
    private String user;

    private Logger logger;
    private String lastInboxPollId = "0";
    private Database database;
    private PriorityQueue<Email> emailQueue = new PriorityQueue<>();

    private boolean running = false;
    private int errorBackoffCount = 0;

    private Gmail gmailService;
    private List<Message> inbox;

    public EmailHandler(String tokensDirectoryPath, String credentialsFilePath, String user, Database database, Logger logger) {
        this.tokensDirectoryPath = tokensDirectoryPath;
        this.credentialsFilePath = credentialsFilePath;
        this.user = user;
        this.database = database;
        this.logger = logger;
    }

    private Credential getCredential(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = EmailHandler.class.getResourceAsStream(credentialsFilePath);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + credentialsFilePath);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(tokensDirectoryPath)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize(user);
    }

    @Override
    public void run() {
        logger.log("Starting email handler...");
        final NetHttpTransport HTTP_TRANSPORT;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            gmailService = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            init(gmailService, user);

        } catch (GeneralSecurityException | IOException e) {
            logger.log(e, "Could not initialize Gmail API");
            running = false;
            return;
        }

        while (running) {
            try {
                pollInbox(gmailService, user);
                processQueue();
                sleep(10000);
                errorBackoffCount = 0;
            } catch (IOException e) {
                logger.log(e, "Gmail API encountered an error");
                if (++errorBackoffCount <= 8) {
                    long backoff = (long) (errorBackoffCount * errorBackoffCount * 1000 + Math.random() * 1000);
                    logger.log(Logger.LogPriority.WARNING, "Reattempting to run Gmail API in " + backoff + " seconds...");
                    try {
                        sleep(backoff);
                    } catch (InterruptedException ex) {
                        logger.log(ex, "Could not sleep thread for backoff, aborting...");
                        running = false;
                    }
                }
            } catch (InterruptedException e) {
                logger.log(e, "Could not sleep thread");
            }
        }
    }

    private void processQueue() {
        while (!emailQueue.isEmpty()) {
            processEmail(emailQueue.poll());
        }
    }

    private void pollInbox(Gmail gmailService, String user) throws IOException {
        inbox = gmailService.users().messages().list(user).execute().getMessages();
        if (!inbox.get(0).getId().equals(lastInboxPollId)) {
            for (Message message : inbox) {
                if (!message.getId().equals(lastInboxPollId)) {
                    emailQueue.add(new Email(gmailService, user, message));
                } else break;
            }

            lastInboxPollId = inbox.get(0).getId();
        }
    }

    public void queueLast() {
        try {
            emailQueue.add(new Email(gmailService, user, inbox.get(0)));
        } catch (IOException e) {
            logger.log(e, "Could not queue latest email in inbox due to IO exception");
        }
    }

    public void queue(int index) {
        try {
            emailQueue.add(new Email(gmailService, user, inbox.get(index)));
        } catch (IOException e) {
            logger.log(e, "Could not queue latest email in inbox due to IO exception");
        }
    }

    private void init(Gmail gmailService, String user) throws IOException {
        lastInboxPollId = gmailService.users().messages().list(user).execute().getMessages().get(0).getId();
        running = true;
    }

    private void processEmail(Email email) {
        logger.log("email.received Received email from " + email.getOrigin() + " with subject \"" + email.getSubject() + "\"");

        AdvancedGuild targetGuild = identifyGuildWithDestination(email.getDestinationUsers().get(0));

        if (targetGuild != null) {
            try {
                if (checkIfAllowed(email, targetGuild)) {
                    announceEmail(email, database.cacheEmailAttachments(email), targetGuild);
                }
            } catch (IOException e) {
                logger.log(e, "Could not cache email attachments");
                announceEmail(email, targetGuild);
            }

        } else {
            logger.log(Logger.LogPriority.WARNING, "No guild found with whitelisted destination \"" + email.getDestination() + "\"");
        }

    }

    private boolean checkIfAllowed(Email email, AdvancedGuild guild) {
        boolean allowed = false;
        if (guild.getDirectAllowAddresses().contains(email.getDestinationUsers().get(0).getAddress())) {
            allowed = true;
            identifyOrigin(email.getOriginUser(), guild);
        } else if (identifyOrigin(email.getOriginUser(), guild)) {
            allowed = true;
        }

        return allowed;
    }

    private boolean identifyOrigin(EmailUser origin, AdvancedGuild guild) {
        boolean allowed = false;
        for (EmailUser user : guild.getWhitelistedEmailOrigins()) {
            if (user.getAddress().equals(origin.getAddress())) {
                allowed = true;
                origin.setEqualTo(user);

                logger.log("set email origin profile image url to " + origin.getProfileImageURL());
                break;
            }
        }

        return allowed;
    }

    private AdvancedGuild identifyGuildWithDestination(EmailUser destination) {
        AdvancedGuild result = null;
        boolean found = false;

        for (AdvancedGuild guild : database.getAdvancedGuilds()) {
            for (EmailUser dest : guild.getWhitelistedEmailDestinations()) {
                if (destination.getAddress().equals(dest.getAddress())) {
                    result = guild;
                    found = true;
                    destination.setEqualTo(dest);
                    break;
                }
            }
            if (!found) {
                for (String dest : guild.getDirectAllowAddresses()) {
                    if (destination.getAddress().equals(dest)) {
                        result = guild;
                        found = true;
                        break;
                    }
                }
            } else {
                break;
            }
        }
        return result;
    }

    private void announceEmail(Email email, ArrayList<String> cachedAttachmentURLs, AdvancedGuild guild) {

        TextChannel targetTextChannel = guild.getAnnouncementsChannel();

        if (targetTextChannel != null) {
            targetTextChannel.sendMessage(Messages.getEmailNotificationMessage(email, guild, cachedAttachmentURLs)).queue();
            guild.getLogChannel().sendMessage(Messages.getEmailAnnouncedLog(email, targetTextChannel)).queue();
            logger.log("bot.event.guild.announcement Announced email from " + email.getOrigin() + " with subject \"" + email.getSubject() + "\"");
        }
    }

    private void announceEmail(Email email, AdvancedGuild guild) {
        announceEmail(email, new ArrayList<>(), guild);
    }

    public String getRecentInboxSummary() throws IOException {
        StringBuilder inboxSummary = new StringBuilder();

        List<Message> inbox = gmailService.users().messages().list(user).execute().getMessages();

        for (int i = 0; i < 10; i++) {
            Email email = new Email(gmailService, user, inbox.get(i));
            inboxSummary.append(email.getOrigin()).append(" ").append(email.getSubject()).append(" ").append(email.getDate()).append(" ").append(email.getMessage().getId());
        }

        return inboxSummary.toString();
    }
}