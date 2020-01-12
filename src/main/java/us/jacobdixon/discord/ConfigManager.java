/*
 * Name: 857-discord-bot
 * Date: 2020/1/11
 * Author(s): jd@jacobdixon.us (Jacob Dixon)
 * Version: 1.0a
 */

package us.jacobdixon.discord;

import com.google.gson.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class ConfigManager {

    public static void loadExtConfigs(String name) throws IOException, ParseException {
        Object obj = new JSONParser().parse(new FileReader(name));
        JSONObject jsonObject = (JSONObject) obj;

        JSONArray approvedSenders = (JSONArray) jsonObject.get("approved-senders");
        Main.knownSenders = new ArrayList<>();

        if(approvedSenders != null) {
            for (Object approvedSender : approvedSenders) {
                EmailSenderProfile esp = new EmailSenderProfile(null, null, null);
                for (Object o : ((Map) approvedSender).entrySet()) {
                    Map.Entry pair = (Map.Entry) o;
                    if (pair.getKey().equals("name")) {
                        esp.setSenderName(pair.getValue().toString());
                    } else if (pair.getKey().equals("address")) {
                        esp.setSenderAddress(pair.getValue().toString());
                    } else if (pair.getKey().equals("img-url")) {
                        esp.setProfileImageUrl(pair.getValue().toString());
                    }
                }
                Main.knownSenders.add(esp);
            }
        }

        JSONArray approvedDestinations = (JSONArray) jsonObject.get("approved-destinations");
        Main.knownDestinations = new ArrayList<>();

        if (approvedDestinations != null) {
            for (Object approvedDest : approvedDestinations) {
                for (Object o : ((Map) approvedDest).entrySet()) {
                    Map.Entry pair = (Map.Entry) o;
                    if (pair.getKey().equals("to")) {
                        Main.knownDestinations.add(pair.getValue().toString());
                    }
                }
            }
        }

        JSONArray emailFilters = (JSONArray) jsonObject.get("message-filters");
        Main.emailFilters = new ArrayList<>();

        if (emailFilters != null) {
            for (Object filter : emailFilters) {
                for (Object o : ((Map) filter).entrySet()) {
                    Map.Entry pair = (Map.Entry) o;
                    if (pair.getKey().equals("filter")) {
                        Main.emailFilters.add(pair.getValue().toString());
                    }
                }
            }
        }

        JSONArray roleAssigners = (JSONArray) jsonObject.get("role-assigners");
        Main.roleAssigners = new ArrayList<>();

        for (Object roleAssigner : roleAssigners) {
            RoleAssigner ra = new RoleAssigner(null, null);
            for (Object o : ((Map) roleAssigner).entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                if (pair.getKey().equals("id")) {
                    ra.setRoleId(pair.getValue().toString());
                } else if (pair.getKey().equals("emote")) {
                    ra.setEmote(pair.getValue().toString());
                }
            }
            Main.roleAssigners.add(ra);
        }

        Main.log("Loaded configs " + name + ".");
    }

    public static void loadConfigs(String name) throws IOException, ParseException {
        if(!new File(name).exists()){
            Main.log(Main.LogPriority.FATAL_ERROR, "No config file found.");
            Main.exit(-1, true);
        }

        Object obj = new JSONParser().parse(new FileReader(name));
        JSONObject jsonObject = (JSONObject) obj;

        Main.announcementsRoleId = jsonObject.get("announcements-role-id").toString();
        Main.adminId = jsonObject.get("admin-id").toString();
        Main.adminRoleId = jsonObject.get("admin-role-id").toString();
        Main.extCacheLocation = jsonObject.get("ext-cache-location").toString();
        Main.announcementsChannelId = jsonObject.get("announcements-channel-id").toString();
        Main.botAdminRoleId = jsonObject.get("bot-admin-role-id").toString();
        Main.roleAssignmentMessageId = jsonObject.get("role-assignment-message-id").toString();
        Main.botToken = jsonObject.get("token").toString();
        Main.cacheLocation = jsonObject.get("cache-location").toString();
        Main.memberRoleId = jsonObject.get("member-role-id").toString();
        Main.domain = jsonObject.get("domain").toString();
        Main.logChannelId = jsonObject.get("log-channel-id").toString();
        Main.serverId = jsonObject.get("server-id").toString();
        Main.status = jsonObject.get("status").toString();
        Main.banListLocation = jsonObject.get("filter-list").toString();

        Main.bannedPhrases = new ArrayList<>();

        Scanner filterConfigScanner = new Scanner(new File(Main.banListLocation));
        while(filterConfigScanner.hasNextLine()){
            String line = filterConfigScanner.nextLine();
            Main.bannedPhrases.add(line);
        }
    }

    public static void saveConfigs(String name) throws FileNotFoundException {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("token", Main.botToken);
        jsonObject.put("cache-location", Main.cacheLocation);
        jsonObject.put("admin-id", Main.adminId);
        jsonObject.put("server-id", Main.serverId);
        jsonObject.put("announcements-channel-id", Main.announcementsChannelId);
        jsonObject.put("log-channel-id", Main.logChannelId);
        jsonObject.put("role-assignment-message-id", Main.roleAssignmentMessageId);
        jsonObject.put("announcements-role-id", Main.announcementsRoleId);
        jsonObject.put("admin-role-id", Main.adminRoleId);
        jsonObject.put("bot-admin-role-id", Main.botAdminRoleId);
        jsonObject.put("member-role-id", Main.memberRoleId);
        jsonObject.put("status", Main.status);
        jsonObject.put("domain", Main.domain);
        jsonObject.put("ext-cache-location", Main.extCacheLocation);

        PrintWriter configWriter = new PrintWriter(name);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = JsonParser.parseString(jsonObject.toJSONString());

        configWriter.write(gson.toJson(jsonElement));
        configWriter.flush();
        configWriter.close();

        PrintWriter filterListWriter = new PrintWriter(Main.banListLocation);

        if(!Main.bannedPhrases.isEmpty()) {
            for (String s : Main.bannedPhrases) {
                filterListWriter.println(s);
            }
        }

        filterListWriter.flush();
        filterListWriter.close();

        Main.log("saved configs " + name + ".");
    }

}
