import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    private static final String TOKEN = "NjYzMTM2ODQzMzgxODY2NTE4.XhEIYg.wOtotE-TllYsFWlMs6JIF3Wk3CQ";

    public static final String ADMIN_ID = "663131245634519040";
    public static final String SERVER_ID = "663131941427609613";
    public static final String ANNOUNCEMENT_CHANNEL_ID = "663172193886142486";
    public static final String ANNOUNCEMENTS_ROLE_ID = "663138202038566924";
    public static final String LOG_CHANNEL_ID = "663166429876584458";

    public static String roleMessageId = "";

    public static EventHandler eventHandler = new EventHandler();
    public static CommandClientBuilder commandClientBuilder = new CommandClientBuilder();

    public static JDA jda;

    public static void main(String[] args) {

        commandClientBuilder.setOwnerId(ADMIN_ID);
        commandClientBuilder.addCommands(
                new CommandsContainer.SayCommand(),
                new CommandsContainer.SetRoleMessageCommand(),
                new CommandsContainer.AnnouncementCommand(),
                new CommandsContainer.PurgeCommand(),
                new CommandsContainer.GenericAnnouncementCommand());
        commandClientBuilder.setPrefix("!");
        commandClientBuilder.setGame(Game.playing("Bot Duties"));
        commandClientBuilder.useHelpBuilder(false);

        try {
            jda = new JDABuilder(AccountType.BOT).setToken(TOKEN).addEventListener(eventHandler, commandClientBuilder.build()).buildBlocking();
        } catch (InterruptedException | LoginException e0) {
            e0.printStackTrace();
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

    public static void embedMessageLog(User auth, String content){
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Private Message Received");
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.addField(new MessageEmbed.Field("Sender","<@" + auth.getId() + ">", false));
        embedBuilder.addField(new MessageEmbed.Field("Message", content, false));
        embedBuilder.setFooter(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()), "https://cdn.discordapp.com/embed/avatars/0.png");

        jda.getGuildById(SERVER_ID).getTextChannelById(LOG_CHANNEL_ID).sendMessage(embedBuilder.build()).queue();
    }

    public static MessageEmbed getEmbed(String auth, String title, String time, String content, String attached){
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(title);
        embedBuilder.setColor(Color.RED);
        embedBuilder.setAuthor(auth, "https://mail.google.com", "https://ssl.gstatic.com/ui/v1/icons/mail/profile_mask2.png");
        embedBuilder.addField(new MessageEmbed.Field("Email Body: ", content, false));
        if (!attached.equals("x")) embedBuilder.addField(new MessageEmbed.Field("Attached: ", attached, false));
        if (time.equals("x")) {
            embedBuilder.setFooter(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), "https://www.jacobdixon.us/res/img/gmail-icon.png");
        } else {
            embedBuilder.setFooter(time, "https://www.jacobdixon.us/res/img/gmail-icon.png");
        }

        return embedBuilder.build();
    }
}
