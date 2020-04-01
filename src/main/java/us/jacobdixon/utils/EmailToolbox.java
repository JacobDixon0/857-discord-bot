/*
 * Author: jd@jacobdixon.us (Jacob R. Dixon)
 * Date: 2020-03-31
 * Project: 857-discord-bot
 * Version: 1.1a
 * Repo: https://github.com/JacobDixon0/857-discord-bot
 */

package us.jacobdixon.utils;

import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Pattern;

public class EmailToolbox {

    public static final SimpleDateFormat GMAIL_DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy, h:mm a");

    public static boolean checkEmailAddress(String emailAddress){
        return Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"" +
                "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-" +
                "\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]" +
                "|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:" +
                "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
                .matcher(emailAddress.toLowerCase()).find();
    }

    public static String getHeaderValue(String headerName, Message message) {
        String headerValue = null;

        for (MessagePartHeader messagePartHeader : message.getPayload().getHeaders()) {
            if (messagePartHeader.getName().toLowerCase().equals(headerName.toLowerCase())) {
                headerValue = messagePartHeader.getValue();
                break;
            }
        }

        if (headerValue == null) {
            throw new IllegalArgumentException("Invalid header name \"" + headerName + "\"");
        }

        return headerValue;
    }

    public static String getPlaintextContent(Message message) {
        StringBuilder stringBuilder = new StringBuilder();

        getPlaintextFromParts(message.getPayload().getParts(), stringBuilder);
        byte[] bodyBytes = Base64.decodeBase64(stringBuilder.toString());
        return new String(bodyBytes, StandardCharsets.UTF_8);
    }

    private static void getPlaintextFromParts(List<MessagePart> messageParts, StringBuilder stringBuilder) {
        for (MessagePart messagePart : messageParts) {
            if (messagePart.getMimeType().equals("text/plain")) {
                stringBuilder.append(messagePart.getBody().getData());
            }

            if (messagePart.getParts() != null) {
                getPlaintextFromParts(messagePart.getParts(), stringBuilder);
            }
        }
    }
}
