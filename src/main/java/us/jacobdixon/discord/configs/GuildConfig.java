package us.jacobdixon.discord.configs;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import us.jacobdixon.discord.EmoteRoleAssignmentMessage;
import us.jacobdixon.discord.email.EmailUser;
import us.jacobdixon.discord.exceptions.InvalidConfigException;
import us.jacobdixon.discord.exceptions.MissingConfigException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GuildConfig extends Config {

    public ConfigEntry<ArrayList<String>> botManagerIDs = new ConfigEntry<>("bot-manager-ids", new ArrayList<>());
    public ConfigEntry<ArrayList<String>> restrictedMentions = new ConfigEntry<>("restricted-mentions", new ArrayList<>());
    public ConfigEntry<ArrayList<String>> bannedPhrases = new ConfigEntry<>("banned-phrases", new ArrayList<>());
    public ConfigEntry<ArrayList<String>> directAllowAddresses = new ConfigEntry<>("direct-allow-addresses", new ArrayList<>());
    public ConfigEntry<String> announcementsChannelID = new ConfigEntry<>("announcements-channel-id", "-");
    public ConfigEntry<String> announcementsRoleID = new ConfigEntry<>("announcements-role-id", "-");
    public ConfigEntry<String> logChannelID = new ConfigEntry<>("log-channel-id", "-");
    public ConfigEntry<Integer> utc = new ConfigEntry<>("utc", 0);
    public ConfigEntry<Boolean> useFilter = new ConfigEntry<>("use-filter", false, false);

    public ConfigEntry<ArrayList<EmoteRoleAssignmentMessage>> emoteRoleAssignmentMessages
            = new ConfigEntry<>("emote-role-assigners", new ArrayList<>(), true);
    public ConfigEntry<ArrayList<EmailUser>> whitelistedEmailOrigins
            = new ConfigEntry<>("whitelisted-origins", new ArrayList<>(), true);
    public ConfigEntry<ArrayList<EmailUser>> whitelistedEmailDestinations
            = new ConfigEntry<>("whitelisted-destinations", new ArrayList<>(), true);

    public GuildConfig() {
        configs = getConfigsList();
    }

    @SuppressWarnings({"rawtypes", "unchecked"}) // I know, I know...
    public void load(File configFile) throws IOException, ParseException, InvalidConfigException {

        if (!checkValidity(configFile)) {
            throw new InvalidConfigException("Invalid guild config file \"" + configFile.getAbsolutePath() + "\"");
        }

        FileReader jsonReader = new FileReader(configFile);

        JSONObject obj = (JSONObject) new JSONParser().parse(jsonReader);

        for (ConfigEntry configEntry : getConfigs()) {
            if (!configEntry.requiresHandling()) {
                if (obj.get(configEntry.getKey()) != null) {
                    configEntry.setValue(obj.get(configEntry.getKey()));
                }
            } else {
                if (configEntry.equals(emoteRoleAssignmentMessages)) {
                    JSONArray raArray = (JSONArray) obj.get(configEntry.getKey());

                    ArrayList<EmoteRoleAssignmentMessage> emoteRoleAssignmentMessages = new ArrayList<>();

                    for (Object ra : raArray) {

                        JSONObject raObj = (JSONObject) ra;
                        HashMap<String, String> emoteRolePairs = new HashMap<>();

                        for (Object pair : (JSONArray) raObj.get("role-emote-pairs")) {
                            JSONObject pairObj = (JSONObject) pair;
                            emoteRolePairs.put(pairObj.get("emote").toString(), pairObj.get("role").toString());
                        }

                        emoteRoleAssignmentMessages.add(new EmoteRoleAssignmentMessage(raObj.get("message-id").toString(), emoteRolePairs));
                    }

                    configEntry.setValue(emoteRoleAssignmentMessages);
                } else if (configEntry.equals(whitelistedEmailOrigins) || configEntry.equals(whitelistedEmailDestinations)) {
                    JSONArray euArray = (JSONArray) obj.get(configEntry.getKey());
                    ArrayList<EmailUser> emailUsers = new ArrayList<>();

                    for (Object eu : euArray) {
                        JSONObject euObj = (JSONObject) eu;
                        EmailUser approvedSender = new EmailUser(euObj.get("address").toString(), euObj.get("name").toString(), euObj.get("profile-image-url").toString());
                        emailUsers.add(approvedSender);
                    }

                    configEntry.setValue(emailUsers);
                }
            }
        }

        loadedConfigFile = configFile;
        jsonReader.close();
    }

    public void load() throws IOException, ParseException, InvalidConfigException, MissingConfigException {
        if (getLoadedConfigFile() != null) {
            load(getLoadedConfigFile());
        } else {
            throw new MissingConfigException("No config file loaded.");
        }
    }

    private ArrayList<ConfigEntry<?>> getConfigsList(){
        return new ArrayList<>(Arrays.asList(utc, botManagerIDs, announcementsChannelID, announcementsRoleID,
                logChannelID, useFilter, whitelistedEmailOrigins, whitelistedEmailDestinations,
                emoteRoleAssignmentMessages, restrictedMentions, bannedPhrases, directAllowAddresses
        ));
    }

    public GuildConfig useDefaults() {
        for (ConfigEntry<?> configEntry : getConfigs()) {
            setConfigValue(configEntry.getKey(), configEntry.getDefaultValue());
        }
        return this;
    }

}
