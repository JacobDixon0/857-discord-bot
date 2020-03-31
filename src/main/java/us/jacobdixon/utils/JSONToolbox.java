package us.jacobdixon.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JSONToolbox {

    public static boolean checkValidity(String jsonString) {

        boolean isValid = true;

        try {
            new JSONObject(jsonString);
        } catch (JSONException e0) {
            try {
                new JSONArray(jsonString);
            } catch (JSONException e1) {
                isValid = false;
            }
        }

        return isValid;
    }

    public static boolean checkValidity(File jsonFile) throws IOException {
        return checkValidity(new String(Files.readAllBytes(Paths.get(jsonFile.getAbsolutePath())), StandardCharsets.UTF_8));
    }

}
