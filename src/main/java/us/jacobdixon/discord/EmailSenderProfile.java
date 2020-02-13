/*
 * Name: 857-discord-bot
 * Date: 2020/1/11
 * Author(s): jd@jacobdixon.us (Jacob Dixon)
 * Version: 1.0a
 */

package us.jacobdixon.discord;

import us.jacobdixon.utils.StringFormatting;

public class EmailSenderProfile {

    private String senderName = "name";
    private String senderAddress = "address";
    private String profileImageUrl = "https://" + Main.config.domain.getValue() + Main.config.extCacheLocation.getValue() + "img/profile_mask2.png";

    public EmailSenderProfile() {
        new EmailSenderProfile(null, null, null);
    }

    public EmailSenderProfile(String senderName, String senderAddress) {
        if (senderName != null) this.senderName = senderName;
        if (senderAddress != null) this.senderAddress = senderAddress.toLowerCase();
    }

    public EmailSenderProfile(String senderName, String senderAddress, String profileImageUrl) {
        if (senderName != null) this.senderName = senderName;
        if (senderAddress != null) this.senderAddress = senderAddress.toLowerCase();
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress.toLowerCase();
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getIdentity() {
        return senderName + " <" + senderAddress.toLowerCase() + ">";
    }

    @Override
    public String toString() {
        return StringFormatting.formatJSON("{\"name\":\"" + senderName + "\",\"address\":\"" + senderAddress + "\",\"img-url\":\"" + profileImageUrl + "\"}");
    }
}
