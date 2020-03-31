package us.jacobdixon.discord.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import us.jacobdixon.discord.exceptions.MissingConfigException;
import us.jacobdixon.utils.JSONToolbox;
import us.jacobdixon.utils.Logger;

import java.io.*;
import java.util.ArrayList;

public class Config {

    File loadedConfigFile;
    ArrayList<ConfigEntry<?>> configs = new ArrayList<>();
    Logger logger = new Logger();

    public ConfigEntry<?> getConfig(String key) {
        ConfigEntry<?> returnConfig = null;
        for (ConfigEntry<?> config : configs) {
            if (config.getKey().equals(key)) {
                returnConfig = config;
            }
        }

        return returnConfig;
    }

    public Object getConfigValue(String key) {
        return getConfig(key).getValue();
    }

    @SuppressWarnings({"rawtypes", "unchecked"}) // I know, I know...
    public <T> void setConfigValue(String key, T value) {
        for (ConfigEntry config : configs) {
            if (config.getKey().equals(key)) {
                config.setValue(value);
            }
        }
    }

    public boolean checkValidity(File file) throws IOException {
        boolean isValid = true;

        if (JSONToolbox.checkValidity(file)) {
            try {
                FileReader jsonReader = new FileReader(file);
                JSONObject jsonObject = (JSONObject) new JSONParser().parse(jsonReader);
                for (ConfigEntry<?> configEntry : configs) {
                    if (!jsonObject.containsKey(configEntry.getKey())) {
                        isValid = false;
                        break;
                    }
                }
                jsonReader.close();
            } catch (ParseException e) {
                isValid = false;
            }
        } else {
            isValid = false;
        }
        return isValid;
    }

    @SuppressWarnings({"unchecked"}) // I know, I know...
    public void save(File configFile) throws FileNotFoundException {

        JSONObject obj = new JSONObject();

        for (ConfigEntry<?> configEntry : configs) {
            obj.put(configEntry.getKey(), configEntry.getValue());
        }

        PrintWriter configWriter = new PrintWriter(configFile);

        Gson gsonFormatter = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        JsonElement jsonElement = JsonParser.parseString(obj.toJSONString());

        configWriter.write(gsonFormatter.toJson(jsonElement));
        configWriter.flush();
        configWriter.close();
    }

    public void save() throws FileNotFoundException, MissingConfigException {
        if (getLoadedConfigFile() != null) {
            save(getLoadedConfigFile());
        } else {
            throw new MissingConfigException("No config file loaded.");
        }
    }

    public ArrayList<ConfigEntry<?>> getConfigs() {
        return configs;
    }

    public File getLoadedConfigFile() {
        return loadedConfigFile;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
