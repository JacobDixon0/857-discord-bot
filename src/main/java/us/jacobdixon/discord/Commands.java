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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Commands {

    public static class EchoCommand extends Command {

        EchoCommand() {
            this.name = "echo";
            this.help = "Posts message.";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_SERVER)) {
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
            this.help = "edits a posted message.";
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
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments");
                }
                if (successfulQuery) event.getMessage().addReaction("\u2705").complete();
            }
        }
    }

    public static class PurgeCommand extends Command {

        PurgeCommand() {
            this.name = "purge";
            this.help = "Clears channel history";
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
            this.help = "Announces a message to #announcements channel.";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_SERVER)) {
                boolean successfulQuery = false;

                String args = event.getArgs();
                String[] argsList = StringFormatting.split(args, Main.config.commandArgDelimiter.getValue());

                if (argsList.length == 6) {
                    List<String> attachmentsList = new ArrayList<>();
                    attachmentsList.add(argsList[5]);
                    EmailSenderProfile sender = new EmailSenderProfile(argsList[0], argsList[1]);
                    for(EmailSenderProfile emailSenderProfile : Main.config.knownSenders.getValue()){
                        if(sender.getSenderAddress().equals(emailSenderProfile.getSenderAddress()) && sender.getSenderName().equals(emailSenderProfile.getSenderName())){
                            sender = emailSenderProfile;
                        }
                    }
                    Main.emailAnnounce(sender, argsList[2], argsList[3], argsList[4], attachmentsList);
                    successfulQuery = true;
                } else {
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments ");
                }
                if (successfulQuery) event.getMessage().addReaction("\u2705").complete();
            }
        }
    }

    public static class EventAnnounceCommand extends Command {

        EventAnnounceCommand() {
            this.name = "eannounce";
            this.help = "posts generic embedded message.";
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
            this.help = "Sets bot mode.";
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
                        Main.config.modeStatus.setValue((long)1);
                        event.getMessage().addReaction("\u2705").complete();
                    } else if (event.getArgs().split(" ")[0].equals("online")) {
                        Main.jda.getPresence().setStatus(OnlineStatus.ONLINE);
                        Main.jda.getPresence().setActivity(Activity.playing(Main.config.activityStatus.getValue()));
                        Main.config.modeStatus.setValue((long)0);
                        event.getMessage().addReaction("\u2705").complete();
                    } else if (event.getArgs().split(" ")[0].equals("disabled")) {
                        Main.jda.getPresence().setActivity(Activity.playing("\u26A0 Limited Functionality"));
                        Main.jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
                        Main.config.modeStatus.setValue((long)2);
                        event.getMessage().addReaction("\u2705").complete();
                    } else{
                        event.getMessage().addReaction("\u274C").queue();
                        event.reply(event.getAuthor().getAsMention() + " Error: Invalid command.");
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
            if (event.getGuild().getMembersWithRoles(event.getGuild().getRoleById(Main.config.botAdminRoleId.getValue())).contains(event.getMember())) {
                event.getMessage().addReaction("\u2705").complete();
                Main.exit(0);
            }
        }
    }

    public static class FilterCommand extends Command {

        private static String[] authorizedRoleIds = new String[]{Main.config.botAdminRoleId.getValue()};

        FilterCommand() {
            this.name = "filter";
            this.help = "Modifies message filters.";
            this.aliases = new String[]{};
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {

            Member author = event.getMember();
            Guild guild = event.getGuild();

            String args = event.getArgs();
            String[] splitArgs = args.split(" ");

            boolean approved = false;

            for (String roleId : authorizedRoleIds) {
                if (guild.getMembersWithRoles(guild.getRoleById(roleId)).contains(author)) {
                    approved = true;
                    break;
                }
            }

            if (approved) {
                boolean successfulQuery = true;

                if (splitArgs[0] == null || splitArgs[0].equals("")) {
                    successfulQuery = false;
                } else if (splitArgs[0].equals("add")) {
                    if (splitArgs[1] != null && !splitArgs[1].matches("\\s") && !splitArgs[1].equals("")) {
                        String filter = args.replaceFirst(splitArgs[0], "").trim();
                        if (!Main.config.bannedPhrases.getValue().contains(filter)) {
                            Main.config.bannedPhrases.getValue().add(filter);
                            Main.reloadConfigs();
                        }
                    }
                } else if (splitArgs[0].equals("list")) {
                    StringBuilder replyBuilder = new StringBuilder();
                    for (String s : Main.config.bannedPhrases.getValue()) {
                        replyBuilder.append("\"").append(s).append("\"\n");
                    }
                    event.reply(event.getAuthor().getAsMention() + "\n" + replyBuilder.toString());
                } else if (splitArgs[0].equals("remove")) {
                    if (splitArgs[1] != null && !splitArgs[1].matches("\\s") && !splitArgs[1].equals("")) {
                        if (splitArgs[1].equals("*")) {
                            Main.config.bannedPhrases.getValue().clear();
                        } else {
                            Main.config.bannedPhrases.getValue().remove(args.replaceFirst(splitArgs[0], "").trim());
                        }
                        Main.reloadConfigs();
                    }
                } else if (splitArgs[0].equals("start")) {
                    Main.config.useFilter.setValue(true);
                } else if (splitArgs[0].equals("stop")) {
                    Main.config.useFilter.setValue(true);
                } else {
                    successfulQuery = false;
                }

                if (successfulQuery) {
                    event.getMessage().addReaction("\u2705").queue();
                } else {
                    event.getMessage().addReaction("\u274C").queue();
                    event.reply(author.getAsMention() + " Error: Invalid command.");
                }
            }
        }
    }

    public static class DebugCommand extends Command {

        private static String[] authorizedRoleIds = new String[]{Main.config.botAdminRoleId.getValue()};

        DebugCommand() {
            this.name = "debug";
            this.help = "Gets bot information.";
            this.aliases = new String[]{};
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {

            Member author = event.getMember();
            Guild guild = event.getGuild();

            String args = event.getArgs();

            boolean approved = false;

            for (String roleId : authorizedRoleIds) {
                if (guild.getMembersWithRoles(guild.getRoleById(roleId)).contains(author)) {
                    approved = true;
                    break;
                }
            }

            if (approved) {
                boolean successfulQuery = true;
                boolean successfulResponse = true;

                switch (args) {
                    case "list dest": {
                        successfulQuery = true;
                        StringBuilder s = new StringBuilder();
                        for (String dest : Main.config.knownDestinations.getValue()) {
                            s.append("<").append(dest).append(">").append("\n");
                        }
                        event.reply(s.toString());
                        break;
                    }
                    case "list send": {
                        successfulQuery = true;
                        StringBuilder s = new StringBuilder();
                        for (EmailSenderProfile sender : Main.config.knownSenders.getValue()) {
                            s.append(sender.getSenderName()).append(" <").append(sender.getSenderAddress()).append(">\n");
                        }
                        event.reply(s.toString());
                        break;
                    }
                    case "config save":
                        successfulResponse = Main.saveConfigs();
                        break;
                    case "config load":
                        successfulResponse = Main.loadConfigs(true);
                        break;
                    case "config reload":
                        successfulResponse = Main.reloadConfigs();
                        break;
                    case "uptime":
                        long uptime = (Instant.now().getEpochSecond() - Main.config.START_TIME.getValue());
                        long days = uptime / 86400;
                        uptime = uptime % 86400;
                        long hours = uptime / 3600;
                        uptime = uptime % 3600;
                        long minutes = uptime / 60;
                        uptime = uptime % 60;
                        long seconds = uptime;
                        StringBuilder replyBuilder = new StringBuilder();
                        if(days > 0) replyBuilder.append(days).append(" days ");
                        if(hours > 0) replyBuilder.append(hours).append(" hours ");
                        if(minutes > 0) replyBuilder.append(minutes).append(" minutes ");
                        if(seconds > 0) replyBuilder.append(seconds).append( " seconds");
                        event.reply(author.getAsMention() + " **Uptime:** " + StringFormatting.normalizeSpacing(replyBuilder.toString()));
                        break;
                    default:
                        successfulQuery = false;
                        break;
                }

                if (successfulQuery && successfulResponse) {
                    event.getMessage().addReaction("\u2705").queue();
                } else if (!successfulQuery) {
                    event.getMessage().addReaction("\u274C").queue();
                    event.reply(author.getAsMention() + " Error: Invalid command.");
                } else if (!successfulResponse) {
                    event.getMessage().addReaction("\u274C").queue();
                    event.reply(author.getAsMention() + " Error: An internal error was encountered while trying to process your request.");
                }
            }
        }
    }
}