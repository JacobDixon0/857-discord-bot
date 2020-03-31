package us.jacobdixon.discord;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import us.jacobdixon.discord.configs.ConfigEntry;
import us.jacobdixon.discord.configs.GuildConfig;
import us.jacobdixon.discord.email.EmailUser;

import java.io.File;
import java.util.ArrayList;

public class AdvancedGuild {

    private Guild guild;
    private GuildConfig config;

    public AdvancedGuild(Guild guild){
        this.guild = guild;
        this.config = new GuildConfig().useDefaults();
    }

    public AdvancedGuild(Guild guild, GuildConfig config){
        this.guild = guild;
        this.config = config;
    }

    public Role getAnnouncementsRole(){
        return guild.getRoleById(getAnnouncementsRoleID());
    }

    public TextChannel getAnnouncementsChannel(){
        return guild.getTextChannelById(getAnnouncementsChannelID());
    }

    public TextChannel getLogChannel(){
        return guild.getTextChannelById(getLogChannelID());
    }

    public String getId(){
        return guild.getId();
    }

    public Guild getGuild() {
        return guild;
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
    }

    public GuildConfig getConfig() {
        return config;
    }

    public void setConfig(GuildConfig config) {
        this.config = config;
    }

    public ArrayList<ConfigEntry<?>> getConfigs() {
        return config.getConfigs();
    }

    public File getLoadedConfigFile() {
        return config.getLoadedConfigFile();
    }

    public ConfigEntry<?> getConfig(String key) {
        return config.getConfig(key);
    }

    public Object getConfigValue(String key) {
        return getConfig(key).getValue();
    }

    public <T> void setConfigValue(String key, T value) {
        config.setConfigValue(key, value);
    }

    public Long getUtc() {
        return config.utc.getValue();
    }

    public void setUtc(Long utc) {
        this.config.utc.setValue(utc);
    }

    public ArrayList<String> getBotManagerIDs() {
        return config.botManagerIDs.getValue();
    }

    public void setBotManagerIDs(ArrayList<String> botManagerIDs) {
        this.config.botManagerIDs.setValue(botManagerIDs);
    }

    public String getAnnouncementsChannelID() {
        return config.announcementsChannelID.getValue();
    }

    public void setAnnouncementsChannelID(String announcementsChannelID) {
        this.config.announcementsChannelID.setValue(announcementsChannelID);
    }

    public String getLogChannelID() {
        return config.logChannelID.getValue();
    }

    public void setLogChannelID(String logChannelID) {
        this.config.logChannelID.setValue(logChannelID);
    }

    public Boolean getUseFilter() {
        return config.useFilter.getValue();
    }

    public void setUseFilter(Boolean useFilter) {
        this.config.useFilter.setValue(useFilter);
    }

    public ArrayList<EmoteRoleAssignmentMessage> getEmoteRoleAssignmentMessages() {
        return config.emoteRoleAssignmentMessages.getValue();
    }

    public void setEmoteRoleAssignmentMessages(ArrayList<EmoteRoleAssignmentMessage> emoteRoleAssignmentMessages) {
        this.config.emoteRoleAssignmentMessages.setValue(emoteRoleAssignmentMessages);
    }

    public ArrayList<String> getRestrictedMentions() {
        return config.restrictedMentions.getValue();
    }

    public void setRestrictedMentions(ArrayList<String> restrictedMentions) {
        this.config.restrictedMentions.setValue(restrictedMentions);
    }

    public ArrayList<EmailUser> getWhitelistedEmailOrigins() {
        return config.whitelistedEmailOrigins.getValue();
    }

    public void setWhitelistedEmailOrigins(ArrayList<EmailUser> whitelistedEmailOrigins) {
        this.config.whitelistedEmailOrigins.setValue(whitelistedEmailOrigins);
    }

    public ArrayList<EmailUser> getWhitelistedEmailDestinations() {
        return config.whitelistedEmailDestinations.getValue();
    }

    public void setWhitelistedEmailDestinations(ArrayList<EmailUser> whitelistedEmailDestinations) {
        this.config.whitelistedEmailDestinations.setValue(whitelistedEmailDestinations);
    }

    public ArrayList<String> getBannedPhrases() {
        return config.bannedPhrases.getValue();
    }

    public void setBannedPhrases(ArrayList<String> bannedPhrases) {
        this.config.bannedPhrases.setValue(bannedPhrases);
    }

    public String getAnnouncementsRoleID() {
        return config.announcementsRoleID.getValue();
    }

    public void setAnnouncementsRoleID(String announcementsRoleID) {
        this.config.announcementsRoleID.setValue(announcementsRoleID);
    }

    public ArrayList<String> getDirectAllowAddresses() {
        return config.directAllowAddresses.getValue();
    }

    public void setDirectAllowAddresses(ArrayList<String> directAllowAddresses) {
        this.config.directAllowAddresses.setValue(directAllowAddresses);
    }
    
}
