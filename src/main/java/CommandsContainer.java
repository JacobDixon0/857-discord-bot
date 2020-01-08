import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommandsContainer {

    public static class SayCommand extends Command {

        SayCommand() {
            this.name = "say";
            this.help = "[Administrative] Posts message.";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_SERVER)) {
                event.reply(event.getArgs());
                event.getMessage().addReaction("\u2705").complete();
            }
        }
    }

    public static class SetRoleMessageCommand extends Command {

        SetRoleMessageCommand() {
            this.name = "srm";
            this.help = "[Administrative] Sets role assignment message.";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_SERVER)) {
                Main.roleMessageId = event.getArgs();
                Main.eventHandler.resetRoleAssigner();
                event.getMessage().addReaction("\u2705").complete();
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
                try{
                    event.getChannel().getHistory().retrievePast(Integer.parseInt(event.getArgs()) + 1).complete().forEach(e -> {
                        try {
                            e.delete().queue();
                        } catch (Exception e0){
                            System.err.println("ERROR: error clearing message: \"" + e.getContentDisplay() + "\"");
                        }
                    });
                    Main.embedPurgeLog("Cleared " + event.getArgs() + " message(s)", event.getChannel());
                } catch (Exception e){
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments");
                }
                event.getMessage().addReaction("\u2705").complete();
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

                if(args.length == 4) {
                    List<String> attachmentsList = new ArrayList<>();
                    attachmentsList.add(args[3]);
                    Main.emailAnnounce(new EmailSenderProfile(args[0], "internal", null), args[1], new SimpleDateFormat("MMM d, yyyy, h:m a").format(new Date()), args[2], attachmentsList);
                } else {
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments "  + args.length);
                }
                event.getMessage().addReaction("\u2705").complete();
            }
        }
    }

    public static class GenericAnnouncementCommand extends Command {

        GenericAnnouncementCommand() {
            this.name = "gannounce";
            this.help = "[Administrative] posts generic embedded message.";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_SERVER)) {

                String[] args = event.getArgs().split("%s%");

                if(args.length == 2) {
                    EmbedBuilder embedBuilder = new EmbedBuilder();

                    embedBuilder.setAuthor(args[0], "https://www.goobisoft.com", "https://cdn0.iconfinder.com/data/icons/small-n-flat/24/678116-calendar-512.png");
                    embedBuilder.addField(new MessageEmbed.Field("Event", args[1], false));

                    event.reply(embedBuilder.build());
                } else {
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments");
                }
                event.getMessage().addReaction("\u2705").complete();
            }
        }
    }
}
