/*
 * Name: 857-discord-bot
 * Date: 2020/1/13
 * Author(s): jd@jacobdixon.us (Jacob Dixon)
 * Version: 1.0a
 */

package us.jacobdixon.discord;

import java.time.Instant;
import java.util.ArrayList;

class Configuration {
    final Config<String> OS_NAME = new Config<>(System.getProperty("os.name").toLowerCase(), "os-name", true);
    final Config<String> RUN_DIR = new Config<>(System.getProperty("user.dir") + "/", "run-dir", true);
    final Config<Long> START_TIME = new Config<>(Instant.now().getEpochSecond(), "start-time", true);

    Config<Boolean> isUnixLike = new Config<>(true, "is-unix-like", true);
    Config<String> hostname = new Config<>("hostname", "hostname", true);

    Config<String> botToken = new Config<>("token");

    Config<String> serverId = new Config<>("server-id");
    Config<String> adminId = new Config<>("admin-id");
    Config<String> botAdminRoleId = new Config<>("bot-admin-role-id");
    Config<String> adminRoleId = new Config<>("admin-role-id");
    Config<String> memberRoleId = new Config<>("member-role-id");
    Config<String> announcementsRoleId = new Config<>("announcements-role-id");
    Config<String> announcementsChannelId = new Config<>("announcements-channel-id");
    Config<String> logChannelId = new Config<>("log-channel-id");
    Config<String> roleAssignmentMessageId = new Config<>("role-assignment-message-id");
    Config<String> domain = new Config<>("domain");
    Config<String> mentorRoleId = new Config<>("mentor-role-id");
    Config<Boolean> useFilter = new Config<>("use-filter");

    Config<String> cacheLocation = new Config<>(RUN_DIR.getValue() + "cache/", "cache-location");
    Config<String> extCacheLocation = new Config<>("cache/", "ext-cache-location");

    Config<String> filterListLocation = new Config<>(RUN_DIR.getValue() + "banlist.txt", "filter-list-location", false, true);
    Config<String> configLocation = new Config<>(RUN_DIR.getValue() + "config.json", "config-location", true);
    Config<String> extConfigLocation = new Config<>(RUN_DIR.getValue() + "ext-config.json", "ext-config-location", true);

    Config<String> activityStatus = new Config<>("Bot Things", "activity");
    Config<String> onlineStatus = new Config<>("online", "online-status");
    Config<Long> modeStatus = new Config<>((long)0, "mode");

    Config<ArrayList<EmailSenderProfile>> knownSenders = new Config<>(new ArrayList<>(), "approved-senders", false, true);
    Config<ArrayList<String>> knownDestinations = new Config<>(new ArrayList<>(), "approved-destinations", false, true);
    Config<ArrayList<RoleAssigner>> roleAssigners = new Config<>(new ArrayList<>(), "role-assigners", false, true);
    Config<ArrayList<String>> emailFilters = new Config<>(new ArrayList<>(), "message-filters", false, true);
    Config<ArrayList<String>> bannedPhrases = new Config<>(new ArrayList<>(), "banned-phrases", true);
    Config<ArrayList<String>> restrictedMentions = new Config<>(new ArrayList<>(), "restricted-mentions", false, true);

    Config<String> commandArgDelimiter = new Config<>(";", "command-arg-delimiter");

    ArrayList<Config<?>> configs = new ArrayList<>();

    public Configuration() {
        configs.add(OS_NAME);
        configs.add(RUN_DIR);
        configs.add(isUnixLike);
        configs.add(hostname);
        configs.add(botToken);
        configs.add(serverId);
        configs.add(adminId);
        configs.add(botAdminRoleId);
        configs.add(adminRoleId);
        configs.add(memberRoleId);
        configs.add(announcementsRoleId);
        configs.add(announcementsChannelId);
        configs.add(logChannelId);
        configs.add(roleAssignmentMessageId);
        configs.add(domain);
        configs.add(cacheLocation);
        configs.add(extCacheLocation);
        configs.add(configLocation);
        configs.add(extConfigLocation);
        configs.add(filterListLocation);
        configs.add(activityStatus);
        configs.add(onlineStatus);
        configs.add(modeStatus);
        configs.add(knownSenders);
        configs.add(knownDestinations);
        configs.add(roleAssigners);
        configs.add(emailFilters);
        configs.add(bannedPhrases);
        configs.add(restrictedMentions);
        configs.add(commandArgDelimiter);
        configs.add(useFilter);
        configs.add(mentorRoleId);
    }

    public ArrayList<Config<?>> getConfigs(){
        return configs;
    }

    public Object getConfigValueByKey(String key){
        Object returnValue = null;
        for(Config<?> config : configs){
            if(config.getKey().equals(key)){
                returnValue = config.getValue();
            }
        }
        return returnValue;
    }

    public <T> void setConfigValueByKey(String key, T value){
        for(Config config : configs){
            if(config.getKey().equals(key)){
                config.setValue(value);
            }
        }
    }
}
