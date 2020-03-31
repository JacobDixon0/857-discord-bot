package us.jacobdixon.discord;

import us.jacobdixon.utils.StringToolbox;

import java.util.HashMap;

public class EmoteRoleAssignmentMessage {

    private String messageID;
    private HashMap<String, String> roleEmotePairs;

    public EmoteRoleAssignmentMessage(String messageID, HashMap<String, String> roleEmotePairs) {
        this.messageID = messageID;
        this.roleEmotePairs = roleEmotePairs;
    }

    public void addRoleEmote(String emoteID, String roleID) {
        roleEmotePairs.put(emoteID, roleID);
    }

    public void removeRoleEmote(String emoteID) {
        roleEmotePairs.remove(emoteID);
    }

    public String getRole(String emoteID) {
        return roleEmotePairs.get(emoteID);
    }

    public String getMessageId() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public HashMap<String, String> getRoleEmotePairs() {
        return roleEmotePairs;
    }

    public void setRoleEmotePairs(HashMap<String, String> roleEmotePairs) {
        this.roleEmotePairs = roleEmotePairs;
    }

    @Override
    public String toString() {
        return toJSON();
    }

    public String toJSON() {
        StringBuilder json = new StringBuilder("{\"message-id\":\"" + messageID + "\",\"role-emote-pairs\":[");

        for (String key : roleEmotePairs.keySet()) {
            json.append("{\"role\":\"").append(roleEmotePairs.get(key)).append("\",\"emote\":\"").append(StringToolbox.sanitize(StringToolbox.escape(key))).append("\"},");
        }

        json.deleteCharAt(json.length() - 1);

        json.append("]}");

        return json.toString();
    }
}
