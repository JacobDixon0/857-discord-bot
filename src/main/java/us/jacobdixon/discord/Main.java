/*
 * Name: 857-discord-bot
 * Date: 2020/1/11
 * Author(s): jd@jacobdixon.us (Jacob Dixon)
 * Version: 1.0a
 */

package us.jacobdixon.discord;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.LoginException;
import java.awt.Color;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    public static final String RUN_DIR = System.getProperty("user.dir");

    public static boolean isUnixLike = true;
    public static String hostname = "hostname";
    static String botToken;

    public static String adminId;
    public static String serverId;
    public static String announcementsChannelId;
    public static String logChannelId;
    public static String roleAssignmentMessageId;
    public static String announcementsRoleId;
    public static String adminRoleId;
    public static String botAdminRoleId;
    public static String memberRoleId;
    public static String domain;

    public static String cacheLocation = RUN_DIR + "/cache/";
    public static String extCacheLocation = "/cache/";

    public static String configLocation = RUN_DIR + "/config.json";
    public static String extConfigLocation = RUN_DIR + "/ext-config.json";
    public static String banListLocation = RUN_DIR + "/banlist.txt";

    public static String status = "Bot Stuff";

    public static EventHandler eventHandler = new EventHandler();
    public static CommandClientBuilder commandClientBuilder = new CommandClientBuilder();

    public static JDA jda;

    public static ArrayList<EmailSenderProfile> knownSenders = new ArrayList<>();
    public static ArrayList<String> knownDestinations = new ArrayList<>();
    public static ArrayList<RoleAssigner> roleAssigners = new ArrayList<>();
    public static ArrayList<String> emailFilters = new ArrayList<>();
    public static ArrayList<String> bannedPhrases = new ArrayList<>();

    public static EmailHandler emailHandler = new EmailHandler();

    public static void main(String[] args) throws IOException {

        loadConfigs();

        commandClientBuilder.setOwnerId(adminId);
        commandClientBuilder.addCommands(
                new Commands.EchoCommand(),
                new Commands.AnnouncementCommand(),
                new Commands.PurgeCommand(),
                new Commands.EventAnnounceCommand(),
                new Commands.ModeCommand(),
                new Commands.PingCommand(),
                new Commands.StopCommand(),
                new Commands.DebugCommand(),
                new Commands.FilterCommand());
        commandClientBuilder.setPrefix("!");
        commandClientBuilder.setActivity(Activity.playing(status));
        commandClientBuilder.useHelpBuilder(false);

        try {
            jda = new JDABuilder(AccountType.BOT).setToken(botToken).addEventListeners(eventHandler, commandClientBuilder.build()).build().awaitReady();
            emailHandler.start();

            embedStartupLog();
            log("Started in " + RUN_DIR + " on " + hostname + " running " + OS_NAME + ".");
        } catch (LoginException | InterruptedException e0) {
            log(e0);
            exit(-1, true);
        }
    }

    public static void cacheFile(String url){
        String name = "file";
        Matcher m = Pattern.compile("^https?://.+(/.+)$").matcher(url);
        if(m.find()){
            if(m.group(1) != null){
                name = m.group(1);
            }
        }
        cacheFile(url, name);
    }

    public static void cacheFile(String url, String name) {
        try {
            ReadableByteChannel bc = Channels.newChannel(new URL(url).openStream());
            FileOutputStream fos = new FileOutputStream(cacheLocation + "/res/" + name);
            FileChannel fc = fos.getChannel();

            fos.getChannel().transferFrom(bc, 0, Long.MAX_VALUE);

            File f = new File(cacheLocation + "/res/" + name);

            if(!name.matches("^.+\\.[a-zA-Z0-9]+$")) {
                String type = URLConnection.guessContentTypeFromStream(new BufferedInputStream(new FileInputStream(f)));

                if (type.equals("image/png")) {
                    boolean success = f.renameTo(new File(cacheLocation + "/res/" + name + ".png"));
                    if (!success)
                        log(LogPriority.ERROR, "Could not rename file extension for file \"" + f.getAbsolutePath() + "\".");
                } else if (type.equals("image/jpeg")) {
                    boolean success = f.renameTo(new File(cacheLocation + "/res/" + name + ".jpg"));
                    if (!success)
                        log(LogPriority.ERROR, "Could not rename file extension for file \"" + f.getAbsolutePath() + "\".");
                }
            }

        } catch (IOException e) {
            log(e);
            log(LogPriority.ERROR, "Could not cache sender profile image");
        }

    }

    public static void reloadConfigs(){
        try {
            loadConfigs();
            ConfigManager.saveConfigs(configLocation);
        } catch (IOException e) {
            log(e);
            log(LogPriority.ERROR, "Could not save configs " + configLocation + "");
        }
    }

    public static void loadConfigs() throws IOException {
        try {
            ConfigManager.loadExtConfigs(extConfigLocation);
            ConfigManager.loadConfigs(configLocation);
        } catch (ParseException e) {
            log(e);
            log(LogPriority.ERROR, "Exception was caught loading configs.");
            exit(-1, true);
        }

        if (OS_NAME.contains("win")) {
            isUnixLike = false;
            hostname = execReadToString("hostname").replace("\n", "").replace("\r", "");
        } else if (OS_NAME.contains("nix") || OS_NAME.contains("nux") || OS_NAME.contains("mac os x")) {
            isUnixLike = true;
            hostname = execReadToString("hostname").replace("\n", "").replace("\r", "");
        }
    }

    public static String execReadToString(String execCommand) throws IOException {
        try (Scanner s = new Scanner(Runtime.getRuntime().exec(execCommand).getInputStream()).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    public static void embedRoleLog(String title, Member member, Role role) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(title);
        embedBuilder.setColor(Color.RED);
        embedBuilder.addField(new MessageEmbed.Field("Member", "<@" + member.getUser().getId() + ">", true));
        embedBuilder.addField(new MessageEmbed.Field("Role", "<@&" + role.getId() + ">", true));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()), "https://" + domain + cacheLocation + "res/discord-logo-blue.png");
        jda.getGuildById(serverId).getTextChannelById(logChannelId).sendMessage(embedBuilder.build()).queue();
    }

    public static void embedPurgeLog(String title, MessageChannel channel) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(title);
        embedBuilder.setColor(Color.RED);
        embedBuilder.addField(new MessageEmbed.Field("Channel", "<#" + channel.getId() + ">", true));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()), "https://" + domain + cacheLocation + "res/discord-logo-blue.png");
        jda.getGuildById(serverId).getTextChannelById(logChannelId).sendMessage(embedBuilder.build()).queue();
    }

    public static void embedFilterLog(Member member, MessageChannel channel, String message, String violation) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Filtered Message");
        embedBuilder.setColor(Color.RED);
        embedBuilder.addField(new MessageEmbed.Field("Channel", "<#" + channel.getId() + ">", true));
        embedBuilder.addField(new MessageEmbed.Field("Member", "<@" + member.getUser().getId() + ">", true));
        embedBuilder.addField(new MessageEmbed.Field("Message", message.replaceAll(violation, "`" + violation + "`"), false));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()), "https://" + domain + cacheLocation + "res/discord-logo-blue.png");
        jda.getGuildById(serverId).getTextChannelById(logChannelId).sendMessage(embedBuilder.build()).queue();
    }

    public static void embedMemberLog(String title, Member member) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(title);
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.addField(new MessageEmbed.Field("Member", "<@" + member.getUser().getId() + ">", true));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()), "https://" + domain + cacheLocation + "res/discord-logo-blue.png");
        jda.getGuildById(serverId).getTextChannelById(logChannelId).sendMessage(embedBuilder.build()).queue();
    }

    public static void embedAnnouncementLog(String sender, String subject) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Posted Email Announcement");
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.addField(new MessageEmbed.Field("Sender", sender, false));
        embedBuilder.addField(new MessageEmbed.Field("Subject", subject, false));
        embedBuilder.addField(new MessageEmbed.Field("Channel", "<#" + announcementsChannelId + ">", false));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()), "https://" + domain + cacheLocation + "res/discord-logo-blue.png");
        jda.getGuildById(serverId).getTextChannelById(logChannelId).sendMessage(embedBuilder.build()).queue();
    }

    public static void embedStartupLog() {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Bot Initiated");
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.addField(new MessageEmbed.Field("Info", "Bot started on host `" + hostname + "`", false));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()), "https://" + domain + cacheLocation + "res/discord-logo-blue.png");
        jda.getGuildById(serverId).getTextChannelById(logChannelId).sendMessage(embedBuilder.build()).queue();
    }

    public static void embedMessageLog(User auth, String content) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Private Message Received");
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.addField(new MessageEmbed.Field("Sender", "<@" + auth.getId() + ">", false));
        embedBuilder.addField(new MessageEmbed.Field("Message", content, false));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()), "https://" + domain + cacheLocation + "res/discord-logo-blue.png");

        jda.getGuildById(serverId).getTextChannelById(logChannelId).sendMessage(embedBuilder.build()).queue();
    }

    public static void emailAnnounce(EmailSenderProfile senderProfile, String title, String time, String content, List<String> attachments) {
        if (title.length() > 50) {
            title = title.substring(0, 40).trim() + "...";
        }
        jda.getGuildById(serverId).getTextChannelById(announcementsChannelId)
                .sendMessage("<@&" + Main.announcementsRoleId + "> Email announcement posted for 857 - \"" + title + "\"").queue();
        jda.getGuildById(serverId).getTextChannelById(announcementsChannelId).sendMessage(getEmailEmbed(senderProfile, title, time, content, attachments)).queue();
        Main.embedAnnouncementLog(senderProfile.getSenderName() + " <" + senderProfile.getSenderAddress() + ">", title);
    }

    public static MessageEmbed getEmailEmbed(EmailSenderProfile senderProfile, String title, String time, String content, List<String> attachments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(title);
        embedBuilder.setColor(Color.RED);
        embedBuilder.setAuthor(senderProfile.getSenderName() + " <" + senderProfile.getSenderAddress() + ">", "https://mail.google.com", senderProfile.getProfileImageUrl());

        if (content.length() > 1024) {
            List<String> contentSections = getChunks(content, 1024);
            embedBuilder.addField("Email Body: ", contentSections.get(0), false);
            for (int i = 1; i < contentSections.size(); i++) {
                embedBuilder.addField("...", contentSections.get(i), false);
            }
        } else {
            embedBuilder.addField(new MessageEmbed.Field("Email Body: ", content, false));
        }

        if (!attachments.isEmpty() && attachments.get(0) != null && !attachments.get(0).equals("x")) {
            StringBuilder sb = new StringBuilder();
            for (String s : attachments) {
                sb.append(s).append("\n");
            }
            embedBuilder.addField(new MessageEmbed.Field("Attached: ", sb.toString(), false));
        }
        if (time.equals("x")) {
            embedBuilder.setFooter(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), "https://" + domain + cacheLocation + "res/gmail-logo.png");
        } else {
            embedBuilder.setFooter(time, "https://" + domain + cacheLocation + "res/gmail-logo.png");
        }

        return embedBuilder.build();
    }

    private static List<String> getChunks(String text, int size) {
        List<String> string = new ArrayList<>();

        for (int i = 0; i < (text.length() / size) + 1; i++) {
            string.add(text.substring(i * 1024, Math.min(text.length(), (i * 1024) + size)));
        }

        return string;
    }

    enum LogPriority {
        INFO, WARNING, ERROR, FATAL_ERROR;
    }

    public static void log(Exception e) {
        System.err.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date()) + " EXCEPTION CAUGHT: ");
        e.printStackTrace();
    }

    public static void log(String message) {
        log(LogPriority.INFO, message);
    }

    public static void log(LogPriority priority, String message) {
        if (priority == LogPriority.INFO) {
            System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date()) + " INFO: " + message);
        } else if (priority == LogPriority.WARNING) {
            System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date()) + " WARNING: " + message);
        } else if (priority == LogPriority.ERROR) {
            System.err.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date()) + " ERROR: " + message);
        } else if (priority == LogPriority.FATAL_ERROR) {
            System.err.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date()) + " FATAL ERROR: " + message);
        }
    }

    public static void exit() {
        exit(0, false);
    }

    public static void exit(int status) {
        exit(status, false);
    }

    public static void exit(int status, boolean force) {
        if (!force) {
            jda.shutdown();
            try {
                ConfigManager.saveConfigs(configLocation);
            } catch (FileNotFoundException e) {
                log(e);
                log(LogPriority.ERROR, "Failed to save configurations before exiting.");
            }
        }
        if (status == 0) {
            log("Exiting...");
        } else {
            log(LogPriority.ERROR, "Exiting due to runtime error...");
        }
        System.exit(status);
    }

}
