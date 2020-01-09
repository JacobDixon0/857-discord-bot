import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Game;
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
                    event.getChannel().getHistory().retrievePast(Integer.parseInt(event.getArgs()) + 1).complete(true).forEach(e -> {
                        try {
                            e.delete().queue();
                        } catch (Exception e0){
                            Main.log("Exception caught attempting to clear message: \"" + e.getContentDisplay() + "\".");
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

                if(args.length == 5) {
                    List<String> attachmentsList = new ArrayList<>();
                    attachmentsList.add(args[4]);
                    Main.emailAnnounce(new EmailSenderProfile(args[0], args[1], null), args[2], new SimpleDateFormat("MMM d, yyyy, h:m a").format(new Date()), args[3], attachmentsList);
                } else {
                    event.reply("<@" + event.getAuthor().getId() + "> Error: Invalid arguments "  + args.length);
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

    public static class ModeCommand extends Command{

        ModeCommand(){
            this.name = "mode";
            this.help = "[Administrative] Sets bot mode.";
            this.hidden = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if(event.getArgs() != null && event.getArgs().split(" ")[0] != null) {
                if (event.getArgs().split(" ")[0].equals("m")) {
                    Main.jda.getPresence().setStatus(OnlineStatus.IDLE);
                    Main.jda.getPresence().setGame(Game.playing("Undergoing Maintenance"));
                } else if (event.getArgs().split(" ")[0].equals("online")){
                    Main.jda.getPresence().setStatus(OnlineStatus.ONLINE);
                    Main.jda.getPresence().setGame(Game.playing(Main.gamePlaying));
                }
            }
        }
    }

}
