import com.google.gson.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class JSONConfigManager {

    public static void loadEmailConfigs(String name) throws IOException, ParseException {
        Object obj = new JSONParser().parse(new FileReader(name));
        JSONObject jsonObject = (JSONObject) obj;
        JSONArray approvedSenders = (JSONArray) jsonObject.get("approved-senders");

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

    }

    public static void loadRoleAssigners(String name) throws IOException, ParseException {
        Object obj = new JSONParser().parse(new FileReader(name));

        JSONObject jsonObject = (JSONObject) obj;

        JSONArray roleAssigners = (JSONArray) jsonObject.get("role-assigners");

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

    }

    public static void saveConfigs(String name) throws FileNotFoundException, ParseException {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("token", Main.botTokenID);
        jsonObject.put("cache", Main.cacheLocation);
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

        PrintWriter pw = new PrintWriter(name);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = JsonParser.parseString(jsonObject.toJSONString());

        pw.write(gson.toJson(jsonElement));

        pw.flush();
        pw.close();

    }

}
