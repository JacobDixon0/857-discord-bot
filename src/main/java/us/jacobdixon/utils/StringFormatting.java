/*
 * Name: 857-discord-bot
 * Date: 2020/1/13
 * Author(s): jd@jacobdixon.us (Jacob Dixon)
 * Version: 1.0a
 */

package us.jacobdixon.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringFormatting {

    public static String[] split(String string, String delim) {
        String[] sections = string.split("(?<!\\\\)" + delim);
        for (int i = 0; i < sections.length; i++) {
            sections[i] = sections[i].replaceAll("(?<!\\\\)\\\\", "");
            sections[i] = sections[i].trim();
        }
        return sections;
    }

    public static String[] split(String string, String delim, String esc) {
        String[] sections;
        if (esc.equals("\\")) {
            sections = split(string, delim);
        } else {
            sections = string.split("(?<!" + esc + ")" + delim);
            for (int i = 0; i < sections.length; i++) {
                sections[i] = sections[i].replaceAll("(?<!" + esc + ")" + esc, "");
                sections[i] = sections[i].trim();
            }
        }
        return sections;
    }

    public static List<String> splitGroups(String string, int length) {
        List<String> s = new ArrayList<>();

        for (int i = 0; i < (string.length() / length) + 1; i++) {
            s.add(string.substring(i * 1024, Math.min(string.length(), (i * 1024) + length)));
        }

        return s;
    }

    public static String formatUrl(String s) {
        return s.replaceAll(" ", "%20").replaceAll(";", "%3B")
                .replaceAll("/", "%2F").replaceAll("\\?", "%3F")
                .replaceAll(":", "%3A").replaceAll("@", "%40")
                .replaceAll("=", "%3D").replaceAll("&", "%26")
                .replaceAll("\"", "%22").replaceAll("#", "%23")
                .replaceAll("~", "%7E").replaceAll("\\\\", "%5C")
                .replaceAll("<", "%3C").replaceAll(">", "%3E")
                .replaceAll("%", "%25").replaceAll("\\^", "%5E")
                .replaceAll("\\{", "%7B").replaceAll("}", "%7D")
                .replaceAll("\\|", "%7C").replaceAll("`", "%80")
                .replaceAll("\\[", "%5B").replaceAll("]", "%5D")
                .replaceAll("'", "%39").replaceAll("\"", "%34");
    }

    public static String formatRegex(String s) {
        s = s.replaceAll("\\\\", "\\\\\\\\");
        Matcher m = Pattern.compile("([\\^\\-\\[\\]*(){}.?+$])").matcher(s);
        while (m.find()) {
            s = m.replaceAll("\\\\$1");
        }
        return s;
    }

    public static String formatJSON(String s) {
        s = s.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\"");
        return s;
    }

    public static String normalizeSpacing(String s) {
        return s.replaceAll(" {2,}", " ").trim();
    }

    public static String unformatEmailTextPlain(String s){
        return s.replaceAll("&#39;", "'").replaceAll("&#34;", "\"");
    }

}
