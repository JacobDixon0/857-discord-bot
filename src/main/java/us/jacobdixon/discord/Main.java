/*
 * Name: 857-discord-bot
 * Date: 2020/1/11
 * Author(s): jd@jacobdixon.us (Jacob Dixon)
 * Version: 1.0a
 */

package us.jacobdixon.discord;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import org.json.simple.parser.ParseException;
import us.jacobdixon.utils.Logger;
import us.jacobdixon.utils.StringFormatting;

import javax.security.auth.login.LoginException;
import java.awt.Color;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static Configuration config = new Configuration();

    public static EventHandler eventHandler = new EventHandler();
    public static EmailHandler emailHandler = new EmailHandler();
    public static CommandClientBuilder commandClientBuilder = new CommandClientBuilder();
    public static Logger logger = new Logger();

    public static JDA jda;

    public static boolean running = false;

    public static void main(String[] args) {

        loadConfigs(false);

        commandClientBuilder.setOwnerId(config.adminId.getValue());
        commandClientBuilder.addCommands(
                new Commands.EchoCommand(),
                new Commands.AnnouncementCommand(),
                new Commands.PurgeCommand(),
                new Commands.EventAnnounceCommand(),
                new Commands.ModeCommand(),
                new Commands.PingCommand(),
                new Commands.StopCommand(),
                new Commands.DebugCommand(),
                new Commands.FilterCommand(),
                new Commands.EchoEditCommand());
        commandClientBuilder.setPrefix("!");
        commandClientBuilder.setActivity(Activity.playing(config.activityStatus.getValue()));
        commandClientBuilder.useHelpBuilder(false);

        try {
            jda = new JDABuilder(AccountType.BOT).setToken(config.botToken.getValue()).addEventListeners(eventHandler, commandClientBuilder.build()).build().awaitReady();
            emailHandler.start();

            loadConfigs(true);
            embedStartupLog();
            running = true;
            logger.log("Started in " + config.RUN_DIR.getValue() + " on " + config.hostname.getValue() + " running " + config.OS_NAME.getValue() + ".");
        } catch (LoginException | InterruptedException e0) {
            logger.log(e0);
            exit(-1, true);
        }
    }

    public static void cacheFile(String url) {
        String name = "file";
        Matcher m = Pattern.compile("^https?://.+(/.+)$").matcher(url);
        if (m.find()) {
            if (m.group(1) != null) {
                name = m.group(1);
            }
        }
        cacheFile(url, name);
    }

    public static void cacheFile(String url, String name) {
        try {
            ReadableByteChannel bc = Channels.newChannel(new URL(url).openStream());
            FileOutputStream fos = new FileOutputStream(config.cacheLocation.getValue() + "/res/" + name);
            FileChannel fc = fos.getChannel();

            fos.getChannel().transferFrom(bc, 0, Long.MAX_VALUE);

            File f = new File(config.cacheLocation.getValue() + "/res/" + name);

            if (!name.matches("^.+\\.[a-zA-Z0-9]+$")) {
                String type = URLConnection.guessContentTypeFromStream(new BufferedInputStream(new FileInputStream(f)));

                if (type.equals("image/png")) {
                    boolean success = f.renameTo(new File(config.cacheLocation.getValue() + "/res/" + name + ".png"));
                    if (!success)
                        logger.log(1, "Could not rename file extension for file \"" + f.getAbsolutePath() + "\".");
                } else if (type.equals("image/jpeg")) {
                    boolean success = f.renameTo(new File(config.cacheLocation.getValue() + "/res/" + name + ".jpg"));
                    if (!success)
                        logger.log(Logger.LogPriority.ERROR, "Could not rename file extension for file \"" + f.getAbsolutePath() + "\".");
                }
            }

        } catch (IOException e) {
            logger.log(e);
            logger.log(Logger.LogPriority.ERROR, "Could not cache sender profile image");
        }
    }

    public static boolean reloadConfigs() {
        return loadConfigs(true) && saveConfigs();
    }

    public static boolean loadConfigs(boolean setBotValues) {
        boolean returnValue = false;
        try {
            ConfigManager.loadExtConfigs(config.extConfigLocation.getValue());
            ConfigManager.loadConfigs(config.configLocation.getValue());
            returnValue = true;
        } catch (ParseException | IOException e) {
            logger.log(e);
            logger.log(0, "Exception was caught loading configs.");
            exit(-1, true);
        }

        if (config.OS_NAME.getValue().toLowerCase().contains("win")) {
            config.isUnixLike.setValue(false);
            try {
                config.hostname.setValue(execReadToString("hostname").replace("\n", "").replace("\r", ""));
            } catch (IOException e) {
                logger.log(e);
                logger.log(1, "Exception was caught discovering hostname.");
            }
        } else if (config.OS_NAME.getValue().toLowerCase().contains("nix") ||
                config.OS_NAME.getValue().toLowerCase().contains("nux") ||
                config.OS_NAME.getValue().toLowerCase().contains("mac os x")) {
            config.isUnixLike.setValue(true);
            try {
                config.hostname.setValue(execReadToString("hostname").replace("\n", "").replace("\r", ""));
            } catch (IOException e) {
                logger.log(e);
                logger.log(1, "Exception was caught discovering hostname.");
            }
        }

        if (running && setBotValues) {
            if (config.modeStatus.getValue() == 0) {
                if (config.onlineStatus.getValue().equals("online")) {
                    jda.getPresence().setStatus(OnlineStatus.ONLINE);
                } else if (config.onlineStatus.getValue().equals("offline")) {
                    jda.getPresence().setStatus(OnlineStatus.OFFLINE);
                } else if (config.onlineStatus.getValue().equals("invisible")) {
                    jda.getPresence().setStatus(OnlineStatus.INVISIBLE);
                } else if (config.onlineStatus.getValue().equals("idle")) {
                    jda.getPresence().setStatus(OnlineStatus.IDLE);
                } else if (config.onlineStatus.getValue().equals("dnd")) {
                    jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
                } else {
                    logger.log(2, "Invalid type set for \"" + config.onlineStatus.getKey() + "\".");
                }
                jda.getPresence().setActivity(Activity.playing(config.activityStatus.getValue()));
                Main.jda.getPresence().setStatus(OnlineStatus.ONLINE);
            } else if (config.modeStatus.getValue() == 1) {
                Main.jda.getPresence().setActivity(Activity.playing("Undergoing Maintenance"));
                Main.jda.getPresence().setStatus(OnlineStatus.IDLE);
            } else if (config.modeStatus.getValue() == 2) {
                Main.jda.getPresence().setActivity(Activity.playing("\u26A0 Limited Functionality"));
                Main.jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
            } else {
                logger.log(2, "Invalid type set for \"" + config.modeStatus.getKey() + "\".");
            }
        }

        return returnValue;
    }

    public static boolean saveConfigs() {
        boolean returnValue = false;
        try {
            ConfigManager.saveConfigs(config.configLocation.getValue());
            returnValue = true;
        } catch (FileNotFoundException e) {
            logger.log(e);
            logger.log(1, "Exception was caught saving configs.");
        }
        return returnValue;
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
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()),
                "https://" + config.domain.getValue() + config.extCacheLocation.getValue() + "res/img/discord-logo-blue.png");
        jda.getGuildById(config.serverId.getValue()).getTextChannelById(config.logChannelId.getValue()).sendMessage(embedBuilder.build()).queue();
    }

    public static void embedPurgeLog(String title, MessageChannel channel) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(title);
        embedBuilder.setColor(Color.RED);
        embedBuilder.addField(new MessageEmbed.Field("Channel", "<#" + channel.getId() + ">", true));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()),
                "https://" + config.domain.getValue() + config.extCacheLocation.getValue() + "res/img/discord-logo-blue.png");
        jda.getGuildById(config.serverId.getValue()).getTextChannelById(config.logChannelId.getValue()).sendMessage(embedBuilder.build()).queue();
    }

    public static void embedFilterLog(Member member, MessageChannel channel, String message, String reason, String violation) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Filtered Message");
        embedBuilder.setColor(Color.RED);
        embedBuilder.addField(new MessageEmbed.Field("Channel", "<#" + channel.getId() + ">", true));
        embedBuilder.addField(new MessageEmbed.Field("Member", "<@" + member.getUser().getId() + ">", true));
        embedBuilder.addField(new MessageEmbed.Field("Reason", reason, true));
        embedBuilder.addField(new MessageEmbed.Field("Message", message.replaceAll(violation, "`" + violation + "`"), false));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()),
                "https://" + config.domain.getValue() + config.extCacheLocation.getValue() + "res/img/discord-logo-blue.png");
        jda.getGuildById(config.serverId.getValue()).getTextChannelById(config.logChannelId.getValue()).sendMessage(embedBuilder.build()).queue();
    }

    public static void embedMemberLog(String title, Member member) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(title);
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.addField(new MessageEmbed.Field("Member", "<@" + member.getUser().getId() + ">", true));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()),
                "https://" + config.domain.getValue() + config.extCacheLocation.getValue() + "res/img/discord-logo-blue.png");
        jda.getGuildById(config.serverId.getValue()).getTextChannelById(config.logChannelId.getValue()).sendMessage(embedBuilder.build()).queue();
    }

    public static void embedAnnouncementLog(String sender, String subject) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Posted Email Announcement");
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.addField(new MessageEmbed.Field("Sender", sender, false));
        embedBuilder.addField(new MessageEmbed.Field("Subject", subject, false));
        embedBuilder.addField(new MessageEmbed.Field("Channel", "<#" + config.announcementsChannelId.getValue() + ">", false));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()),
                "https://" + config.domain.getValue() + config.extCacheLocation.getValue() + "res/img/discord-logo-blue.png");
        jda.getGuildById(config.serverId.getValue()).getTextChannelById(config.logChannelId.getValue()).sendMessage(embedBuilder.build()).queue();
    }

    public static void embedStartupLog() {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Bot Initiated");
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.addField(new MessageEmbed.Field("Info", "Bot started on host `" + config.hostname.getValue() + "`", false));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()),
                "https://" + config.domain.getValue() + config.extCacheLocation.getValue() + "res/img/discord-logo-blue.png");
        jda.getGuildById(config.serverId.getValue()).getTextChannelById(config.logChannelId.getValue()).sendMessage(embedBuilder.build()).queue();
    }

    public static void embedMessageLog(User auth, String content) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Private Message Received");
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.addField(new MessageEmbed.Field("Sender", "<@" + auth.getId() + ">", false));
        embedBuilder.addField(new MessageEmbed.Field("Message", content, false));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()),
                "https://" + config.domain.getValue() + config.extCacheLocation.getValue() + "res/img/discord-logo-blue.png");

        jda.getGuildById(config.serverId.getValue()).getTextChannelById(config.logChannelId.getValue()).sendMessage(embedBuilder.build()).queue();
    }

    public static void emailAnnounce(EmailSenderProfile senderProfile, String title, String time, String content, List<String> attachments) {
        if (title.length() > 50) {
            title = title.substring(0, 40).trim() + "...";
        }

        jda.getGuildById(config.serverId.getValue()).getTextChannelById(config.announcementsChannelId.getValue())
                .sendMessage("<@&" + config.announcementsRoleId.getValue() + "> Email announcement posted for 857 - \"" + title + "\"").queue();
        jda.getGuildById(config.serverId.getValue()).getTextChannelById(config.announcementsChannelId.getValue()).
                sendMessage(getEmailEmbed(senderProfile, title, time, content, attachments)).queue();
        Main.embedAnnouncementLog(senderProfile.getIdentity(), title);
    }

    public static MessageEmbed getEmailEmbed(EmailSenderProfile senderProfile, String title, String time, String content, List<String> attachments) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(title);
        embedBuilder.setColor(Color.RED);
        embedBuilder.setAuthor(senderProfile.getIdentity(), "https://mail.google.com", senderProfile.getProfileImageUrl());

        if (content.length() > 1024) {
            List<String> contentSections = StringFormatting.splitGroups(content, 1024);
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
            embedBuilder.setFooter(new SimpleDateFormat("MMM d, yyyy, h:m a").format(new Date()),
                    "https://" + config.domain.getValue() + config.extCacheLocation.getValue() + "res/img/gmail-icon.png");
        } else {
            embedBuilder.setFooter(time, "https://" + config.domain.getValue() + config.extCacheLocation.getValue() + "res/img/gmail-icon.png");
        }

        return embedBuilder.build();
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
                ConfigManager.saveConfigs(config.configLocation.getValue());
            } catch (FileNotFoundException e) {
                logger.log(e);
                logger.log(1, "Failed to save configurations before exiting.");
            }
            running = false;
        }
        if (status == 0) {
            logger.log("Exiting...");
        } else {
            logger.log("Exiting due to runtime error...");
        }
        System.exit(status);
    }

}
