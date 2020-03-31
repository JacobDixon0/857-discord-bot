package us.jacobdixon.discord.email;

import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import us.jacobdixon.discord.files.SimpleFile;
import us.jacobdixon.utils.EmailToolbox;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Email {
    private Message message;

    private String origin;
    private String destination;
    private String subject;
    private String plaintextContent;
    private String date;

    private EmailUser originUser;
    private ArrayList<us.jacobdixon.discord.email.EmailUser> destinationUsers;

    private ArrayList<SimpleFile> attachments = new ArrayList<>();

    private static final SimpleDateFormat ORIGINAL_DATE_FORMAT = new SimpleDateFormat("EEE, d MMM hh:mm:ss Z");

    public Email(Gmail gmailService, String user, Message message) throws IOException {
        message = gmailService.users().messages().get(user, message.getId()).execute();
        this.message = message;

        origin = EmailToolbox.getHeaderValue("from", message);
        destination = EmailToolbox.getHeaderValue("to", message);
        subject = EmailToolbox.getHeaderValue("subject", message);

        try {
            date = EmailToolbox.GMAIL_DATE_FORMAT.format(ORIGINAL_DATE_FORMAT.parse(EmailToolbox.getHeaderValue("date", message)).getTime());
        } catch (ParseException e) {
            date = EmailToolbox.getHeaderValue("date", message);
        }

        originUser = new us.jacobdixon.discord.email.EmailUser(origin);
        destinationUsers = new ArrayList<>();

        String[] splitArray = destination.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        for(String dest : splitArray){
            dest = dest.trim();
            destinationUsers.add(new EmailUser(dest));
        }

        plaintextContent = EmailToolbox.getPlaintextContent(message);

        for (MessagePart messagePart : message.getPayload().getParts()) {
            if (messagePart.getFilename() != null && messagePart.getFilename().length() > 0) {
                if (messagePart.getBody().getAttachmentId() == null) break;
                MessagePartBody attachmentPart = gmailService.users().messages().attachments().get(user, message.getId(), messagePart.getBody().getAttachmentId()).execute();
                attachments.add(new SimpleFile(messagePart.getFilename(), Base64.decodeBase64(attachmentPart.getData())));
            }
        }
    }

    public Message getMessage() {
        return message;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String getSubject() {
        return subject;
    }

    public String getPlaintextContent() {
        return plaintextContent;
    }

    public String getDate() {
        return date;
    }

    public String getDate(int utcOffset) throws ParseException {
        return EmailToolbox.GMAIL_DATE_FORMAT.format(ORIGINAL_DATE_FORMAT.parse(EmailToolbox.getHeaderValue("date", message)).getTime() + utcOffset * 3600000);
    }

    public long getDateEpoch() throws ParseException {
        return ORIGINAL_DATE_FORMAT.parse(EmailToolbox.getHeaderValue("date", message)).getTime();
    }

    public us.jacobdixon.discord.email.EmailUser getOriginUser() {
        return originUser;
    }

    public ArrayList<us.jacobdixon.discord.email.EmailUser> getDestinationUsers() {
        return destinationUsers;
    }

    public ArrayList<SimpleFile> getAttachments() {
        return attachments;
    }
}
