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

        JSONArray approvedSenders = (JSONArray) jsonObject.get(Main.config.knownSenders.getKey());
        Main.config.knownSenders.setValue(new ArrayList<>());

        if (approvedSenders != null) {
            for (Object approvedSender : approvedSenders) {
                EmailSenderProfile esp = new EmailSenderProfile();
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
                Main.config.knownSenders.getValue().add(esp);
            }
        }

        JSONArray approvedDestinations = (JSONArray) jsonObject.get(Main.config.knownDestinations.getKey());
        Main.config.knownDestinations.setValue(new ArrayList<>());

        if (approvedDestinations != null) {
            for (Object approvedDest : approvedDestinations) {
                for (Object o : ((Map) approvedDest).entrySet()) {
                    Map.Entry pair = (Map.Entry) o;
                    if (pair.getKey().equals("to")) {
                        Main.config.knownDestinations.getValue().add(pair.getValue().toString());
                    }
                }
            }
        }

        JSONArray emailFilters = (JSONArray) jsonObject.get(Main.config.emailFilters.getKey());
        Main.config.emailFilters.setValue(new ArrayList<>());

        if (emailFilters != null) {
            for (Object filter : emailFilters) {
                for (Object o : ((Map) filter).entrySet()) {
                    Map.Entry pair = (Map.Entry) o;
                    if (pair.getKey().equals("filter")) {
                        Main.config.emailFilters.getValue().add(pair.getValue().toString());
                    }
                }
            }
        }

        JSONArray roleAssigners = (JSONArray) jsonObject.get(Main.config.roleAssigners.getKey());
        Main.config.roleAssigners.setValue(new ArrayList<>());

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
            Main.config.roleAssigners.getValue().add(ra);
        }

        JSONArray restrictedMentions = (JSONArray) jsonObject.get(Main.config.restrictedMentions.getKey());
        Main.config.restrictedMentions.setValue(new ArrayList<>());

        for (Object restriction : restrictedMentions) {
            for (Object o : ((Map) restriction).entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                if (pair.getKey().equals("id")) {
                    Main.config.restrictedMentions.getValue().add(pair.getValue().toString());
                }
            }
        }
        Main.logger.log("Loaded configs " + name + ".");
    }

    public static void loadConfigs(String name) throws IOException, ParseException {
        if (!new File(name).exists()) {
            Main.logger.log(0, "No config file found.");
            Main.exit(-1, true);
        }

        Object obj = new JSONParser().parse(new FileReader(name));
        JSONObject jsonObject = (JSONObject) obj;

        for (Config<?> config : Main.config.getConfigs()) {
            if (!config.isVolatile() && jsonObject.get(config.getKey()) != null) {
                Main.config.setConfigValueByKey(config.getKey(), jsonObject.get(config.getKey()));
            }
        }

        Main.config.bannedPhrases.setValue(new ArrayList<>());

        Scanner filterConfigScanner = new Scanner(new File(Main.config.filterListLocation.getValue()));
        while (filterConfigScanner.hasNextLine()) {
            String line = filterConfigScanner.nextLine();
            if (!line.equals("") && !line.matches("^\\s+$")) {
                Main.config.bannedPhrases.getValue().add(line);
            }
        }

        for (Config<?> config : Main.config.configs) {
            if (config.getValue() == null) {
                Main.logger.log(0, "Missing config \"" + config.getKey() + "\" in " + Main.config.configLocation.getValue() + ".");
                Main.exit(-1, true);
            }
        }
    }

    public static void saveConfigs(String name) throws FileNotFoundException {

        JSONObject jsonObject = new JSONObject();

        for (Config<?> config : Main.config.getConfigs()) {
            if (!config.isVolatile() && !config.isExternal()) {
                jsonObject.put(config.getKey(), config.getValue());
            }
        }

        PrintWriter configWriter = new PrintWriter(name);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = JsonParser.parseString(jsonObject.toJSONString());

        configWriter.write(gson.toJson(jsonElement));
        configWriter.flush();
        configWriter.close();

        PrintWriter filterListWriter = new PrintWriter(Main.config.filterListLocation.getValue());

        if (!Main.config.bannedPhrases.getValue().isEmpty()) {
            for (String s : Main.config.bannedPhrases.getValue()) {
                filterListWriter.println(s);
            }
        } else {
            filterListWriter.println();
        }

        filterListWriter.flush();
        filterListWriter.close();

        Main.logger.log("saved configs " + name + ".");
    }

}
