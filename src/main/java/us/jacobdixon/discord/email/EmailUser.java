/*
 * Author: jd@jacobdixon.us (Jacob R. Dixon)
 * Date: 2020-03-31
 * Project: 857-discord-bot
 * Version: 1.1a
 * Repo: https://github.com/JacobDixon0/857-discord-bot
 */

package us.jacobdixon.discord.email;

import us.jacobdixon.utils.EmailToolbox;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailUser {

    private String address;
    private String name;
    private String profileImageURL = "https://www.jacobdixon.us/cache/static/profile_mask2.png";

    public EmailUser(String address, String name, String profileImageURL) {
        this.address = address.toLowerCase();
        this.name = name;
        this.profileImageURL = profileImageURL;
    }

    public EmailUser(String address, String name) {
        this.address = address.toLowerCase();
        this.name = name;
    }

    public EmailUser(String identity) {
        setIdentity(identity);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    public void setProfileImageURL(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }

    public void setIdentity(String name, String address) {
        this.name = name;
        this.address = address.toLowerCase();
    }

    public void setIdentity(String identity) {
        Matcher identityParser = Pattern.compile("^(.+) <(.+)>$").matcher(identity);
        if (identityParser.find()) {
            if (EmailToolbox.checkEmailAddress(identityParser.group(2))) {
                name = identityParser.group(1);
                address = identityParser.group(2).toLowerCase();
            } else {
                throw new IllegalArgumentException("Invalid email address \"" + identityParser.group(2) + "\"");
            }
        } else if (EmailToolbox.checkEmailAddress(identity)) {
            this.address = identity;
        } else {
            throw new IllegalArgumentException("Invalid email address \"" + identity + "\"");
        }
    }

    public String getIdentity() {
        String identity;
        if (name != null && !name.equals("")) {
            identity = name + " <" + address + ">";
        } else {
            identity = address;
        }
        return identity;
    }

    public void setEqualTo(EmailUser emailUser) {
        this.setName(emailUser.getName());
        this.setAddress(emailUser.getAddress());
        this.setProfileImageURL(emailUser.getProfileImageURL());
    }

    @Override
    public String toString() {
        return toJSON();
    }

    public String toJSON() {
        return "{\"address\":\"" + address + "\",\"name\":\"" + name + "\",\"profile-image-url\":\"" + profileImageURL + "\"}";
    }
}
