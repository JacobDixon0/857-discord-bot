package us.jacobdixon.discord.configs;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import us.jacobdixon.discord.exceptions.InvalidConfigException;
import us.jacobdixon.discord.exceptions.MissingConfigException;
import us.jacobdixon.utils.Logger;
import us.jacobdixon.utils.SystemToolbox;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

public class GlobalConfig extends Config {

    public final String RUN_DIR = FileSystems.getDefault().getPath("").toAbsolutePath().toString() + "/";
    public final String USER_DIR = System.getProperty("user.dir") + "/";
    public final String OS_NAME = System.getProperty("os.name");
    public final long INIT_TIME = Instant.now().getEpochSecond();

    public static boolean isUnixLike;
    public static String hostname;

    public ConfigEntry<String> superAdmin = new ConfigEntry<>("super-admin");
    public ConfigEntry<String> databaseLocation = new ConfigEntry<>("db-location");
    public ConfigEntry<String> domain = new ConfigEntry<>("domain");
    public ConfigEntry<String> token = new ConfigEntry<>("token");
    public ConfigEntry<String> globalLogChannelID = new ConfigEntry<>("global-log-channel-id");
    public ConfigEntry<String> prefix = new ConfigEntry<>("command-prefix");
    public ConfigEntry<String> delimiter = new ConfigEntry<>("command-arg-delimiter");
    public ConfigEntry<String> timestampFormat = new ConfigEntry<>("timestamp-format");
    public ConfigEntry<String> tokensPath = new ConfigEntry<>("tokens-path");
    public ConfigEntry<String> credentialsPath = new ConfigEntry<>("credentials-path");
    public ConfigEntry<String> gmailUser = new ConfigEntry<>("gmail-user");
    public ConfigEntry<String> streamingURL = new ConfigEntry<>("streaming-url");
    public ConfigEntry<Long> onlineStatus = new ConfigEntry<>("online-status");
    public ConfigEntry<Long> mode = new ConfigEntry<>("mode");
    public ConfigEntry<Long> activityType = new ConfigEntry<>("activity-type");

    public ConfigEntry<ArrayList<String>> emailFilters = new ConfigEntry<>("email-filters");
    public ConfigEntry<ArrayList<String>> activityEntries = new ConfigEntry<>("activity-entries");
    public ConfigEntry<ArrayList<String>> adminIDs = new ConfigEntry<>("admin-ids");

    public GlobalConfig() {
        getConfigs().addAll(Arrays.asList(superAdmin, databaseLocation, domain, token, globalLogChannelID,
                prefix, onlineStatus, timestampFormat, emailFilters, activityEntries, adminIDs, delimiter,
                mode, tokensPath, credentialsPath, gmailUser, activityType, streamingURL));

        if (OS_NAME.toLowerCase().contains("win")) {
            isUnixLike = false;
        } else if (OS_NAME.toLowerCase().matches("(?:.+)?(?:nix|nux|mac os x)(?:.+)?")) {
            isUnixLike = true;
        } else {
            logger.log(Logger.LogPriority.WARNING, "Could not determine operating system type");
        }

        try {
            hostname = SystemToolbox.execReadToString("hostname").replaceAll("[\n\r]", "");
        } catch (IOException e) {
            logger.log(e, "Could not get system hostname");
            hostname = "unknown_hostname";
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"}) // I know, I know...
    public void load(File configFile) throws IOException, ParseException, InvalidConfigException {
        logger.log("Loading global config...");

        try{
            checkValidity(configFile);
        } catch (Exception e){
            logger.log(e);
            throw new InvalidConfigException("Invalid guild config \"" + configFile.getAbsolutePath() + "\"");
        }

        FileReader jsonReader = new FileReader(configFile.toString());

        JSONObject obj = (JSONObject) new JSONParser().parse(jsonReader);

        for (ConfigEntry configEntry : getConfigs()) {
            if (!configEntry.requiresHandling()) {
                if (obj.get(configEntry.getKey()) != null) {
                    configEntry.setValue(obj.get(configEntry.getKey()));
                }
            }
        }

        loadedConfigFile = configFile;
        jsonReader.close();
    }

    public void load() throws IOException, ParseException, MissingConfigException, InvalidConfigException {
        if (getLoadedConfigFile() != null) {
            load(getLoadedConfigFile());
        } else {
            throw new MissingConfigException("No config file loaded.");
        }
    }

    public static boolean isIsUnixLike() {
        return isUnixLike;
    }

    public static String getHostname() {
        return hostname;
    }

    public String getSuperAdmin() {
        return superAdmin.getValue();
    }

    public void setSuperAdmin(String superAdmin) {
        this.superAdmin.setValue(superAdmin);
    }

    public String getDatabaseLocation() {
        return databaseLocation.getValue();
    }

    public void setDatabaseLocation(String databaseLocation) {
        this.databaseLocation.setValue(databaseLocation);
    }

    public String getDomain() {
        return domain.getValue();
    }

    public void setDomain(String domain) {
        this.domain.setValue(domain);
    }

    public String getToken() {
        return token.getValue();
    }

    public void setToken(String token) {
        this.token.setValue(token);
    }

    public String getGlobalLogChannelID() {
        return globalLogChannelID.getValue();
    }

    public void setGlobalLogChannelID(String globalLogChannelID) {
        this.globalLogChannelID.setValue(globalLogChannelID);
    }

    public String getPrefix() {
        return prefix.getValue();
    }

    public void setPrefix(String prefix) {
        this.prefix.setValue(prefix);
    }

    public String getDelimiter() {
        return delimiter.getValue();
    }

    public void setDelimiter(String delimiter) {
        this.delimiter.setValue(delimiter);
    }

    public String getTimestampFormat() {
        return timestampFormat.getValue();
    }

    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat.setValue(timestampFormat);
    }

    public Long getOnlineStatus() {
        return onlineStatus.getValue();
    }

    public void setOnlineStatus(Long onlineStatus) {
        this.onlineStatus.setValue(onlineStatus);
    }

    public Long getMode() {
        return mode.getValue();
    }

    public void setMode(Long mode) {
        this.mode.setValue(mode);
    }

    public ArrayList<String> getActivityEntries() {
        return activityEntries.getValue();
    }

    public void setActivityEntries(ArrayList<String> activityEntries) {
        this.activityEntries.setValue(activityEntries);
    }

    public ArrayList<String> getAdminIDs() {
        return adminIDs.getValue();
    }

    public void setAdminIDs(ArrayList<String> adminIDs) {
        this.adminIDs.setValue(adminIDs);
    }

    public ArrayList<String> getEmailFilters() {
        return emailFilters.getValue();
    }

    public void setEmailFilters(ArrayList<String> emailFilters) {
        this.adminIDs.setValue(emailFilters);
    }

    public String getTokensPath() {
        return tokensPath.getValue();
    }

    public void setTokensPath(String tokensPath) {
        this.tokensPath.setValue(tokensPath);
    }

    public String getCredentialsPath() {
        return credentialsPath.getValue();
    }

    public void setCredentialsPath(String credentialsPath) {
        this.credentialsPath.setValue(credentialsPath);
    }

    public String getGmailUser() {
        return gmailUser.getValue();
    }

    public void setGmailUser(String gmailUser) {
        this.gmailUser.setValue(gmailUser);
    }

    public Long getActivityType() {
        return activityType.getValue();
    }

    public void setActivityType(Long activityType) {
        this.activityType.setValue(activityType);
    }

    public String getStreamingURL() {
        return streamingURL.getValue();
    }

    public void setStreamingURL(String streamingURL) {
        this.streamingURL.setValue(streamingURL);
    }

}
