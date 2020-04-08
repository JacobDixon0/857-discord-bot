/*
 * Author: jd@jacobdixon.us (Jacob R. Dixon)
 * Date: 2020-03-31
 * Project: 857-discord-bot
 * Version: 1.1a
 * Repo: https://github.com/JacobDixon0/857-discord-bot
 */

/*
 * Name: 857-discord-bot
 * Date: 2020/1/13
 * Author(s): jd@jacobdixon.us (Jacob Dixon)
 * Version: 1.0a
 */

package us.jacobdixon.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringToolbox {

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

    public static ArrayList<String> splitGroups(String string, int length) {
        ArrayList<String> s = new ArrayList<>();

        for (int i = 0; i < (string.length() / length) + 1; i++) {
            s.add(string.substring(i * 1024, Math.min(string.length(), (i * 1024) + length)));
        }

        return s;
    }

    public static String sanitizeURL(String s) {
        return s.replaceAll("%", "%25").replaceAll(" ", "%20")
                .replaceAll(";", "%3B").replaceAll("/", "%2F")
                .replaceAll("\\?", "%3F").replaceAll(":", "%3A")
                .replaceAll("@", "%40").replaceAll("=", "%3D")
                .replaceAll("&", "%26").replaceAll("\"", "%22")
                .replaceAll("#", "%23").replaceAll("~", "%7E")
                .replaceAll("\\\\", "%5C").replaceAll("<", "%3C")
                .replaceAll(">", "%3E").replaceAll("\\^", "%5E")
                .replaceAll("\\{", "%7B").replaceAll("}", "%7D")
                .replaceAll("\\|", "%7C").replaceAll("`", "%80")
                .replaceAll("\\[", "%5B").replaceAll("]", "%5D")
                .replaceAll("'", "%39").replaceAll("\"", "%34");
    }

    public static String sanitizeRegex(String s) {
        s = s.replaceAll("\\\\", "\\\\\\\\");
        Matcher m = Pattern.compile("([\\^\\-\\[\\]*(){}.?+$])").matcher(s);
        while (m.find()) {
            s = m.replaceAll("\\\\$1");
        }
        return s;
    }

    public static String sanitizeJSON(String s) {
        s = s.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\"");
        return s;
    }

    public static String sanitizeEmailPlaintext(String s) {
        return s.replaceAll("'", "&#39;").replaceAll("\"", "&#34;");
    }

    public static String desanitizeEmailPlaintext(String s) {
        return s.replaceAll("&#39;", "'").replaceAll("&#34;", "\"");
    }

    public static String sanitize(String s) {
        return s.replace("\\", "\\\\");
    }

    public static String normalizeSpacing(String s) {
        return s.replaceAll(" {2,}", " ").trim();
    }

    public static String formatTime(long time) {
        long years = time / 31556952;
        time = time % 31556952;
        long months = time / 2592000;
        time = time % 2592000;
        long weeks = time / 604800;
        time = time % 604800;
        long days = time / 86400;
        time = time % 86400;
        long hours = time / 3600;
        time = time % 3600;
        long minutes = time / 60;
        time = time % 60;

        long seconds = time;

        StringBuilder stringBuilder = new StringBuilder();

        if (years > 1) stringBuilder.append(years).append(" years ");
        else if (years == 1) stringBuilder.append(years).append(" year ");
        if (months > 1) stringBuilder.append(months).append(" months ");
        else if (months == 1) stringBuilder.append(months).append(" month ");
        if (weeks > 1) stringBuilder.append(weeks).append(" weeks ");
        else if (weeks == 1) stringBuilder.append(weeks).append(" week ");
        if (days > 1) stringBuilder.append(days).append(" days ");
        else if (days == 1) stringBuilder.append(days).append(" day ");
        if (hours > 1) stringBuilder.append(hours).append(" hours ");
        else if (hours == 1) stringBuilder.append(hours).append(" hour ");
        if (minutes > 1) stringBuilder.append(minutes).append(" minutes ");
        else if (minutes == 1) stringBuilder.append(minutes).append(" minute ");
        if (seconds > 1) stringBuilder.append(seconds).append(" seconds ");
        else if (seconds == 1) stringBuilder.append(seconds).append(" second");

        return normalizeSpacing(stringBuilder.toString());
    }

    public static String normalizeRelativePath(String path, boolean isDirectory) {
        path = path.replaceAll("\\\\", "/").replaceAll("/{2,}", "/");
        if (!path.startsWith("/")) path = "/" + path;
        if (isDirectory && !path.endsWith("/")) path += "/";
        return path;
    }

    public static String escape(String string) {
        return string.chars().mapToObj(StringToolbox::escapeChar).collect(Collectors.joining());
    }

    private static String escapeChar(int c) {
        if (c <= 0x7f) {
            return Character.toString((char) c);
        } else {
            return "\\u" + String.format("%04x", c).toUpperCase();
        }
    }

}
