import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;

import javax.security.auth.login.LoginException;
import java.awt.Color;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    public static boolean isUnixLike = true;
    public static String hostname = "hostname";

    private static final String BOT_ACCOUNT_TOKEN = "NjYzMTM2ODQzMzgxODY2NTE4.XhEIYg.wOtotE-TllYsFWlMs6JIF3Wk3CQ";

    public static final String ADMIN_ID = "663131245634519040";
    public static final String SERVER_ID = "663131941427609613";
    public static final String ANNOUNCEMENT_CHANNEL_ID = "663172193886142486";
    public static final String ANNOUNCEMENTS_ROLE_ID = "663138202038566924";
    public static final String LOG_CHANNEL_ID = "663166429876584458";
    public static final String ROLE_ASSIGNMENT_MESSAGE_ID = "663167436127993884";
    public static final String ADMIN_ROLE_ID = "663133216470728714";
    public static final String MEMBER_ROLE_ID = "663133335660265498";

    public static final String CACHE_LOCATION = "/var/www/html/cache/";

    public static String roleMessageId = ROLE_ASSIGNMENT_MESSAGE_ID;
    public static String gamePlaying = "Bot Duties";

    public static EventHandler eventHandler = new EventHandler();
    public static CommandClientBuilder commandClientBuilder = new CommandClientBuilder();

    public static JDA jda;

    public static final ArrayList<EmailSenderProfile> knownSenders = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        knownSenders.add(new EmailSenderProfile("Jacob Dixon", "jd@jacobdixon.us", "https://lh3.googleusercontent.com/a-/AAuE7mDWDAfX7W_d7M-vC4O1VDWZIDHPZ_ji7b7dze-B=s40"));
        knownSenders.add(new EmailSenderProfile("Christopher Doig", "csdoig@mtu.edu", "https://ssl.gstatic.com/ui/v1/icons/mail/profile_mask2.png"));

        if(OS_NAME.contains("win")){
            isUnixLike = false;
            hostname = execReadToString("hostname");
        } else if (OS_NAME.contains("nix") || OS_NAME.contains("nux") || OS_NAME.contains("mac os x")){
            isUnixLike = true;
            hostname = execReadToString("hostname");
        }

        commandClientBuilder.setOwnerId(ADMIN_ID);
        commandClientBuilder.addCommands(
                new CommandsContainer.SayCommand(),
                new CommandsContainer.SetRoleMessageCommand(),
                new CommandsContainer.AnnouncementCommand(),
                new CommandsContainer.PurgeCommand(),
                new CommandsContainer.EventAnnounceCommand(),
                new CommandsContainer.ModeCommand());
        commandClientBuilder.setPrefix("!");
        commandClientBuilder.setGame(Game.playing(gamePlaying));
        commandClientBuilder.useHelpBuilder(false);

        try {
            jda = new JDABuilder(AccountType.BOT).setToken(BOT_ACCOUNT_TOKEN).addEventListener(eventHandler, commandClientBuilder.build()).buildBlocking();
            GmailAPIHandler emailHandler = new GmailAPIHandler();
            emailHandler.start();
            embedStartupLog();
        } catch (InterruptedException | LoginException e0) {
            e0.printStackTrace();
        }
    }

    public static String execReadToString(String execCommand) throws IOException{
        try (Scanner s = new Scanner(Runtime.getRuntime().exec(execCommand).getInputStream()).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    public static void embedRoleLog(String title, Member member, Role role){
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(title);
        embedBuilder.setColor(Color.RED);
        embedBuilder.addField(new MessageEmbed.Field("Member", "<@" + member.getUser().getId() + ">", true));
        embedBuilder.addField(new MessageEmbed.Field("Role", "<@&" + role.getId() + ">", true));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()), "https://cdn.discordapp.com/embed/avatars/0.png");
        jda.getGuildById(SERVER_ID).getTextChannelById(LOG_CHANNEL_ID).sendMessage(embedBuilder.build()).queue();
    }

    public static void embedPurgeLog(String title, MessageChannel channel){
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(title);
        embedBuilder.setColor(Color.RED);
        embedBuilder.addField(new MessageEmbed.Field("Channel", "<#" + channel.getId() + ">", true));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()), "https://cdn.discordapp.com/embed/avatars/0.png");
        jda.getGuildById(SERVER_ID).getTextChannelById(LOG_CHANNEL_ID).sendMessage(embedBuilder.build()).queue();
    }

    public static void embedMemberLog(String title, Member member){
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(title);
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.addField(new MessageEmbed.Field("Member", "<@" + member.getUser().getId() + ">", true));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()), "https://cdn.discordapp.com/embed/avatars/0.png");
        jda.getGuildById(SERVER_ID).getTextChannelById(LOG_CHANNEL_ID).sendMessage(embedBuilder.build()).queue();
    }

    public static void embedAnnouncementLog(String sender, String subject){
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Posted Email Announcement");
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.addField(new MessageEmbed.Field("Sender", sender, false));
        embedBuilder.addField(new MessageEmbed.Field("Subject", subject, false));
        embedBuilder.addField(new MessageEmbed.Field("Channel", "<#" + ANNOUNCEMENT_CHANNEL_ID + ">", false));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()), "https://cdn.discordapp.com/embed/avatars/0.png");
        jda.getGuildById(SERVER_ID).getTextChannelById(LOG_CHANNEL_ID).sendMessage(embedBuilder.build()).queue();
    }

    public static void embedStartupLog(){
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Bot Initiated");
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.addField(new MessageEmbed.Field("Info", "Bot started on host `" + hostname + "`", false));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()), "https://cdn.discordapp.com/embed/avatars/0.png");
        jda.getGuildById(SERVER_ID).getTextChannelById(LOG_CHANNEL_ID).sendMessage(embedBuilder.build()).queue();
    }

    public static void embedMessageLog(User auth, String content){
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Private Message Received");
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.addField(new MessageEmbed.Field("Sender","<@" + auth.getId() + ">", false));
        embedBuilder.addField(new MessageEmbed.Field("Message", content, false));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()), "https://cdn.discordapp.com/embed/avatars/0.png");

        jda.getGuildById(SERVER_ID).getTextChannelById(LOG_CHANNEL_ID).sendMessage(embedBuilder.build()).queue();
    }

    public static void emailAnnounce(EmailSenderProfile senderProfile, String title, String time, String content, List<String> attachments){
        if (title.length() > 50){
            title = title.substring(0, 40).trim() + "...";
        }
        jda.getGuildById(SERVER_ID).getTextChannelById(ANNOUNCEMENT_CHANNEL_ID)
                .sendMessage("<@&" + Main.ANNOUNCEMENTS_ROLE_ID + "> Email announcement posted for 857 - \"" + title + "\"").queue();
        jda.getGuildById(SERVER_ID).getTextChannelById(ANNOUNCEMENT_CHANNEL_ID).sendMessage(getEmailEmbed(senderProfile, title, time, content, attachments)).queue();
        Main.embedAnnouncementLog(senderProfile.getSenderName() + " <" + senderProfile.getSenderAddress() + ">", title);
    }

    public static MessageEmbed getEmailEmbed(EmailSenderProfile senderProfile, String title, String time, String content, List<String> attachments){
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(title);
        embedBuilder.setColor(Color.RED);
        embedBuilder.setAuthor(senderProfile.getSenderName() + " <" + senderProfile.getSenderAddress() + ">", "https://mail.google.com", senderProfile.getProfileImageUrl());

        if(content.length() > 1024){
            List<String> contentSections = getChunks(content, 1024);
            embedBuilder.addField("Email Body: ", contentSections.get(0), false);
            for (int i = 1; i < contentSections.size(); i++) {
                embedBuilder.addField("...", contentSections.get(i), false);
            }
        } else {
            embedBuilder.addField(new MessageEmbed.Field("Email Body: ", content, false));
        }

        if (!attachments.isEmpty() && attachments.get(0) != null && !attachments.get(0).equals("x")){
            StringBuilder sb = new StringBuilder();
            for(String s : attachments){
                sb.append(s).append("\n");
            }
            embedBuilder.addField(new MessageEmbed.Field("Attached: ", sb.toString(), false));
        }
        if (time.equals("x")) {
            embedBuilder.setFooter(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), "https://www.jacobdixon.us/res/img/gmail-icon.png");
        } else {
            embedBuilder.setFooter(time, "https://www.jacobdixon.us/res/img/gmail-icon.png");
        }

        return embedBuilder.build();
    }

    private static List<String> getChunks(String text, int size){

        List<String> string = new ArrayList<>();

        for(int i = 0; i < (text.length() / size) + 1; i++){
            string.add(text.substring(i * 1024, Math.min(text.length(), (i * 1024) + size)));
        }

        return string;
    }

    enum LogPriority{
        INFO, WARNING, ERROR;
    }

    public static void log(Exception e){
        System.err.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date()) + " EXCEPTION CAUGHT: ");
        e.printStackTrace();
    }

    public static void log(String message){
        log(LogPriority.INFO, message);
    }

    public static void log(LogPriority priority, String message){
        if(priority == LogPriority.INFO){
            System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date()) + " INFO: " + message);
        } else if (priority == LogPriority.WARNING){
            System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date()) + " WARNING: " + message);
        } else if (priority == LogPriority.ERROR){
            System.err.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date()) + " ERROR: " + message);
        }
    }



}
