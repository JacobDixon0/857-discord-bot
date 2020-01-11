import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommandsContainer {

    public static class EchoCommand extends Command {

        EchoCommand() {
            this.name = "echo";
            this.help = "[Administrative] Posts message.";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_SERVER) &&
                    event.getGuild().getMembersWithRoles(event.getGuild().getRoleById(Main.adminRoleId)).contains(event.getMember())) {
                boolean successfulQuery = false;
                String[] args = event.getArgs().split(" ");
                if(args[0].matches("<#\\d+>")){
                    try {
                        event.getGuild().getTextChannelById(args[0].replaceAll("[<#>]", "")).sendMessage(event.getArgs().replace(args[0], "")).queue();
                        successfulQuery = true;
                    } catch (Exception e){
                        Main.log(e);
                        Main.log(Main.LogPriority.ERROR, "Exception caught while echoing message.");
                        event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments");
                    }
                } else {
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments");
                }
                if(successfulQuery) event.getMessage().addReaction("\u2705").complete();
            }
        }
    }

    public static class PurgeCommand extends Command {

        PurgeCommand() {
            this.name = "purge";
            this.help = "[Administrative] Clears channel history";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MESSAGE_MANAGE)) {
                try {
                    event.getChannel().getHistory().retrievePast(Integer.parseInt(event.getArgs()) + 1).complete(true).forEach(e -> {
                        try {
                            e.delete().queue();
                        } catch (Exception e0) {
                            Main.log("Exception caught attempting to clear message: \"" + e.getContentDisplay() + "\".");
                        }
                    });
                    Main.embedPurgeLog("Cleared " + event.getArgs() + " message(s)", event.getChannel());
                } catch (Exception e) {
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments");
                }
            }
        }
    }

    public static class AnnouncementCommand extends Command {

        AnnouncementCommand() {
            this.name = "announce";
            this.help = "[Administrative] Announces a message to #announcements channel.";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_SERVER)) {

                String[] args = event.getArgs().split("%s%");

                if (args.length == 5) {
                    List<String> attachmentsList = new ArrayList<>();
                    attachmentsList.add(args[4]);
                    Main.emailAnnounce(new EmailSenderProfile(args[0], args[1], null), args[2], new SimpleDateFormat("MMM d, yyyy, h:m a").format(new Date()), args[3], attachmentsList);
                } else {
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments " + args.length);
                }
                event.getMessage().addReaction("\u2705").complete();
            }
        }
    }

    public static class EventAnnounceCommand extends Command {

        EventAnnounceCommand() {
            this.name = "eannounce";
            this.help = "[Administrative] posts generic embedded message.";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_SERVER)) {

                String[] args = event.getArgs().split("%s%");

                if (args.length == 2) {
                    EmbedBuilder embedBuilder = new EmbedBuilder();

                    embedBuilder.setAuthor(args[0], "https://calendar.google.com/", "https://www.jacobdixon.us/cache/res/calendar-icon.png");
                    embedBuilder.addField(new MessageEmbed.Field("Event", args[1], false));

                    event.reply(embedBuilder.build());
                } else {
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments");
                }
                event.getMessage().addReaction("\u2705").complete();
            }
        }
    }

    public static class ModeCommand extends Command {

        ModeCommand() {
            this.name = "mode";
            this.help = "[Administrative] Sets bot mode.";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getGuild().getMembersWithRoles(event.getGuild().getRoleById(Main.botAdminRoleId)).contains(event.getMember())) {
                if (event.getArgs() != null && event.getArgs().split(" ")[0] != null) {
                    if (event.getArgs().split(" ")[0].equals("m")) {
                        Main.jda.getPresence().setActivity(Activity.playing("Undergoing Maintenance"));
                        Main.jda.getPresence().setStatus(OnlineStatus.IDLE);
                        event.getMessage().addReaction("\u2705").complete();
                    } else if (event.getArgs().split(" ")[0].equals("online")) {
                        Main.jda.getPresence().setStatus(OnlineStatus.ONLINE);
                        Main.jda.getPresence().setActivity(Activity.playing(Main.status));
                        event.getMessage().addReaction("\u2705").complete();
                    }
                }
            }
        }
    }

    public static class PingCommand extends Command {

        PingCommand() {
            this.name = "ping";
            this.help = "Tests bot status";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            event.getMessage().addReaction("\uD83C\uDFD3").complete();
        }
    }

    public static class StopCommand extends Command {

        StopCommand() {
            this.name = "stop";
            this.help = "Shuts down bot.";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getGuild().getMembersWithRoles(event.getGuild().getRoleById(Main.botAdminRoleId)).contains(event.getMember())) {
                event.getMessage().addReaction("\u2705").complete();
                Main.exit(0);
            }
        }
    }

    public static class DebugCommand extends Command {

        DebugCommand() {
            this.name = "debug";
            this.help = "Gets bot information.";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getGuild().getMembersWithRoles(event.getGuild().getRoleById(Main.botAdminRoleId)).contains(event.getMember())) {
                boolean successfulQuery = false;
                if (event.getArgs().equals("senders")) {
                    StringBuilder s = new StringBuilder();
                    for (EmailSenderProfile emailSenderProfile : Main.knownSenders) {
                        s.append(emailSenderProfile.getSenderName()).append(" <").append(emailSenderProfile.getSenderAddress()).append(">\n");
                    }
                    event.reply(s.toString());
                    successfulQuery = true;
                } else if (event.getArgs().equals("destinations")) {
                    StringBuilder s = new StringBuilder();
                    for (String dest : Main.knownDestinations) {
                        s.append("<").append(dest).append(">").append("\n");
                    }
                    event.reply(s.toString());
                    successfulQuery = true;
                } else if (event.getArgs().equals("saveconfigs")) {
                    try {
                        JSONConfigManager.saveConfigs(Main.configLocation);
                        successfulQuery = true;
                        Main.log("Saved config file " + Main.configLocation);
                    } catch (ParseException | FileNotFoundException e) {
                        Main.log(e);
                        Main.log(Main.LogPriority.ERROR, "Failed to save configs.");
                    }
                } else if (event.getArgs().equals("loadconfigs")) {
                    try {
                        Main.loadConfigs();
                        successfulQuery = true;
                        Main.log("Loaded config file " + Main.configLocation);
                    } catch (IOException e) {
                        Main.log(e);
                        Main.log(Main.LogPriority.ERROR, "Failed to load configs.");
                    }
                }
                if (successfulQuery) event.getMessage().addReaction("\u2705").complete();
            }
        }
    }

}