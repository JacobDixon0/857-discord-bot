/*
 * Name: 857-discord-bot
 * Date: 2020/1/11
 * Author(s): jd@jacobdixon.us (Jacob Dixon)
 * Version: 1.0a
 */

package us.jacobdixon.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import us.jacobdixon.utils.StringFormatting;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Commands {

    public static class EchoCommand extends Command {

        EchoCommand() {
            this.name = "echo";
            this.help = "[Administrative] Posts message.";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_SERVER) &&
                    event.getGuild().getMembersWithRoles(event.getGuild().getRoleById(Main.config.adminRoleId.getValue())).contains(event.getMember())) {
                boolean successfulQuery = false;
                String args = event.getArgs();
                if (args.equals("")) {
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments");
                } else {
                    String[] argsList = args.split(" ");
                    if (argsList[0].matches("<#\\d+>")) {
                        try {
                            event.getGuild().getTextChannelById(argsList[0].replaceAll("[<#>]", "")).sendMessage(event.getArgs().replace(argsList[0], "")).queue();
                            successfulQuery = true;
                        } catch (Exception e) {
                            Main.log(e);
                            Main.log(Main.LogPriority.ERROR, "Exception caught while echoing message.");
                            event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments");
                        }
                    } else {
                        event.reply(event.getArgs());
                        successfulQuery = true;
                    }
                }
                if (successfulQuery) event.getMessage().addReaction("\u2705").complete();
            }
        }
    }

    public static class EchoEditCommand extends Command {

        EchoEditCommand() {
            this.name = "eecho";
            this.help = "[Administrative] edits a posted message.";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_SERVER)) {
                boolean successfulQuery = false;
                String args = event.getArgs();
                String[] argsList = args.split(" ");
                try {
                    event.getGuild().getTextChannelById(argsList[0].replaceAll("[<#>]", "")).editMessageById(argsList[1], args.replace(argsList[0] + " " + argsList[1] + " ", "")).queue();
                    successfulQuery = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments");
                }
                if (successfulQuery) event.getMessage().addReaction("\u2705").complete();
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
                boolean successfulQuery = false;

                String args = event.getArgs();
                String[] argsList = StringFormatting.split(args, Main.config.commandArgDelimiter.getValue());

                if (argsList.length == 5) {
                    List<String> attachmentsList = new ArrayList<>();
                    attachmentsList.add(argsList[4]);
                    Main.emailAnnounce(new EmailSenderProfile(argsList[0], argsList[1], null), argsList[2], new SimpleDateFormat("MMM d, yyyy, h:m a").format(new Date()), argsList[3], attachmentsList);
                    successfulQuery = true;
                } else {
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments " + argsList.length);
                }
                if (successfulQuery) event.getMessage().addReaction("\u2705").complete();
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
                boolean successfulQuery = false;

                String args = event.getArgs();
                String[] argsList = StringFormatting.split(args, Main.config.commandArgDelimiter.getValue());
                Message message = event.getMessage();

                if (!message.getMentionedChannels().isEmpty() && argsList.length == 4) {
                    TextChannel channel = message.getMentionedChannels().get(0);

                    EmbedBuilder embedBuilder = new EmbedBuilder();

                    embedBuilder.setAuthor(argsList[1], "https://calendar.google.com/", "https://www.jacobdixon.us/cache/res/calendar-icon.png");
                    embedBuilder.setTitle(argsList[2]);
                    embedBuilder.addField(new MessageEmbed.Field("Event", argsList[3], false));

                    channel.sendMessage(embedBuilder.build()).queue();
                    successfulQuery = true;
                } else {
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments");
                }
                if (successfulQuery) event.getMessage().addReaction("\u2705").complete();
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
            if (event.getGuild().getMembersWithRoles(event.getGuild().getRoleById(Main.config.botAdminRoleId.getValue())).contains(event.getMember())) {
                if (event.getArgs().equals(""))
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments");
                if (event.getArgs() != null && event.getArgs().split(" ")[0] != null) {
                    if (event.getArgs().split(" ")[0].equals("m")) {
                        Main.jda.getPresence().setActivity(Activity.playing("Undergoing Maintenance"));
                        Main.jda.getPresence().setStatus(OnlineStatus.IDLE);
                        event.getMessage().addReaction("\u2705").complete();
                    } else if (event.getArgs().split(" ")[0].equals("online")) {
                        Main.jda.getPresence().setStatus(OnlineStatus.ONLINE);
                        Main.jda.getPresence().setActivity(Activity.playing(Main.config.status.getValue()));
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
            this.help = "[Administrative] Shuts down bot.";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getGuild().getMembersWithRoles(event.getGuild().getRoleById(Main.config.botAdminRoleId.getValue())).contains(event.getMember())) {
                event.getMessage().addReaction("\u2705").complete();
                Main.exit(0);
            }
        }
    }

    public static class FilterCommand extends Command {

        FilterCommand() {
            this.name = "filter";
            this.help = "[Administrative] Modifies message filters";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getGuild().getMembersWithRoles(event.getGuild().getRoleById(Main.config.botAdminRoleId.getValue())).contains(event.getMember())) {
                String args = event.getArgs();
                String[] argsList = args.split(" ");
                if (event.getArgs().equals(""))
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments");
                boolean successfulQuery = false;

                if (argsList[0] == null) {
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments");
                } else if (argsList[0].equals("add")) {
                    if (argsList[1] != null) {
                        if (!Main.config.bannedPhrases.getValue().contains(args.replaceFirst(argsList[0] + " ", ""))) {
                            Main.config.bannedPhrases.getValue().add(args.replaceFirst(argsList[0] + " ", ""));
                            Main.reloadConfigs();
                        }
                        successfulQuery = true;
                    } else {
                        event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments");
                    }
                } else if (argsList[0].equals("remove")) {
                    if (argsList[1] != null) {
                        if (argsList[1].equals("*")) {
                            Main.config.bannedPhrases.getValue().clear();
                        } else {
                            Main.config.bannedPhrases.getValue().remove(args.replaceFirst(argsList[0] + " ", ""));
                        }
                        Main.reloadConfigs();
                        successfulQuery = true;
                    } else {
                        event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments");
                    }
                } else if (argsList[0].equals("list")) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : Main.config.bannedPhrases.getValue()) {
                        sb.append("\"").append(s).append("\"\n");
                    }
                    event.reply(sb.toString());
                    successfulQuery = true;
                } else if (argsList[0].equals("start")) {
                    Main.config.useFilter.setValue(true);
                    successfulQuery = true;
                } else if (argsList[0].equals("stop")) {
                    Main.config.useFilter.setValue(false);
                    successfulQuery = true;
                }
                if (successfulQuery) event.getMessage().addReaction("\u2705").complete();
            }
        }
    }

    public static class DebugCommand extends Command {

        DebugCommand() {
            this.name = "debug";
            this.help = "[Administrative] Gets bot information.";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getGuild().getMembersWithRoles(event.getGuild().getRoleById(Main.config.botAdminRoleId.getValue())).contains(event.getMember())) {
                if (event.getArgs().equals(""))
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments");
                boolean successfulQuery = false;
                if (event.getArgs().equals("senders")) {
                    StringBuilder s = new StringBuilder();
                    for (EmailSenderProfile emailSenderProfile : Main.config.knownSenders.getValue()) {
                        s.append(emailSenderProfile.getSenderName()).append(" <").append(emailSenderProfile.getSenderAddress()).append(">\n");
                    }
                    event.reply(s.toString());
                    successfulQuery = true;
                } else if (event.getArgs().equals("destinations")) {
                    StringBuilder s = new StringBuilder();
                    for (String dest : Main.config.knownDestinations.getValue()) {
                        s.append("<").append(dest).append(">").append("\n");
                    }
                    event.reply(s.toString());
                    successfulQuery = true;
                } else if (event.getArgs().equals("saveconfigs")) {
                    try {
                        ConfigManager.saveConfigs(Main.config.configLocation.getValue());
                        successfulQuery = true;
                    } catch (FileNotFoundException e) {
                        Main.log(e);
                        Main.log(Main.LogPriority.ERROR, "Failed to save configs.");
                    }
                } else if (event.getArgs().equals("loadconfigs")) {
                    try {
                        Main.loadConfigs();
                        successfulQuery = true;
                    } catch (IOException e) {
                        Main.log(e);
                        Main.log(Main.LogPriority.ERROR, "Failed to load configs.");
                    }
                } else if (event.getArgs().equals("reloadconfigs")) {
                    Main.reloadConfigs();
                    successfulQuery = true;

                } else if (event.getArgs().equals("bp")) {
                    StringBuilder reply = new StringBuilder();
                    for (String s : Main.config.bannedPhrases.getValue()) {
                        reply.append("\"").append(s).append("\"").append("\n");
                    }
                    event.reply(reply.toString());
                    successfulQuery = true;
                }
                if (successfulQuery) event.getMessage().addReaction("\u2705").complete();
            }
        }
    }
}