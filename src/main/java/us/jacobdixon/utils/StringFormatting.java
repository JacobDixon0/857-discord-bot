/*
 * Name: 857-discord-bot
 * Date: 2020/1/13
 * Author(s): jd@jacobdixon.us (Jacob Dixon)
 * Version: 1.0a
 */

package us.jacobdixon.utils;

public class StringFormatting {

    public static String[] split(String string, String delim){
        String[] sections = string.split("(?<!\\\\)" + delim);
        for(int i = 0; i < sections.length; i++){
            sections[i] = sections[i].replaceAll("(?<!\\\\)\\\\", "");
            sections[i] = sections[i].trim();
        }
        return sections;
    }

    public static String[] split(String string, String delim, String esc){
        String[] sections;
        if(esc.equals("\\")) {
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
}
