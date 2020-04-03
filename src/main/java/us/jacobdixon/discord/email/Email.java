/*
 * Author: jd@jacobdixon.us (Jacob R. Dixon)
 * Date: 2020-03-31
 * Project: 857-discord-bot
 * Version: 1.1a
 * Repo: https://github.com/JacobDixon0/857-discord-bot
 */

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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

public class Email {
    private Message message;

    private String origin;
    private String destination;
    private String subject;
    private String plaintextContent;
    private String date;

    private long dateEpoch;

    private EmailUser originUser;
    private ArrayList<EmailUser> destinationUsers;

    private ArrayList<SimpleFile> attachments = new ArrayList<>();

    public Email(Gmail gmailService, String user, Message message) throws IOException {
        message = gmailService.users().messages().get(user, message.getId()).execute();
        this.message = message;

        origin = EmailToolbox.getHeaderValue("from", message);
        destination = EmailToolbox.getHeaderValue("to", message);
        subject = EmailToolbox.getHeaderValue("subject", message);
        date = EmailToolbox.getHeaderValue("date", message);

        try {
            dateEpoch = EmailToolbox.DATE_FORMAT.parse(date).getTime() / 1000;
            date = EmailToolbox.GMAIL_DATE_FORMAT.format(new Date(EmailToolbox.DATE_FORMAT.parse(date).getTime()));
        } catch (ParseException e) {
            dateEpoch = Instant.now().getEpochSecond();
            e.printStackTrace();
        }

        originUser = new EmailUser(origin);
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
        return EmailToolbox.GMAIL_DATE_FORMAT.format(new Date(EmailToolbox.DATE_FORMAT.parse(EmailToolbox.getHeaderValue("date", message)).getTime() + utcOffset * 3600000));
    }

    public long getDateEpoch() {
        return dateEpoch;
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
