import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventHandler extends ListenerAdapter {

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if(!event.getAuthor().getId().equals(Main.jda.getSelfUser().getId())) {
            Main.embedMessageLog(event.getAuthor(), event.getMessage().getContentDisplay());
            Main.log("Received private message from \"" + event.getAuthor().getName() + "\" : \"" + event.getMessage().getContentDisplay() + "\".");
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if(event.getGuild().getId().equals(Main.serverId)) {
            if(event.getGuild().getId().equals(Main.serverId)) {
                event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(Main.memberRoleId)).queue();
            }
        }
        Main.embedMemberLog("Member Joined", event.getMember());
        Main.log("Member joined: \"" + event.getMember().getEffectiveName() + "\".");
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        Main.embedMemberLog("Member Left", event.getMember());
        Main.log("Member left: \"" + event.getMember().getEffectiveName() + "\".");
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        for(RoleAssigner role : Main.roleAssigners){
            if(event.getMessageId().equals(Main.roleAssignmentMessageId) && event.getReactionEmote().getEmoji().equals(role.getEmote())){
                if(event.getGuild().getId().equals(Main.serverId)) {
                    event.getGuild().addRoleToMember(event.getGuild().getMemberById(event.getUser().getId()), event.getGuild().getRoleById(role.getRoleId())).queue(success -> {
                        event.getMember().getUser().openPrivateChannel().queue(privateChannel -> {
                            privateChannel.sendMessage("Assigned Role: " + event.getGuild().getRoleById(role.getRoleId()).getName()).queue();
                        });
                    });
                    Main.embedRoleLog("Assigned Role", event.getMember(), event.getGuild().getRoleById(role.getRoleId()));
                    Main.log("Assigned role: \"" + event.getGuild().getRoleById(role.getRoleId()).getName() + "\" to: \"" + event.getMember().getEffectiveName() + "\".");
                }
            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
        for(RoleAssigner role : Main.roleAssigners){
            if(event.getMessageId().equals(Main.roleAssignmentMessageId) && event.getReactionEmote().getEmoji().equals(role.getEmote())){
                if(event.getGuild().getId().equals(Main.serverId)) {
                    event.getGuild().removeRoleFromMember(event.getGuild().getMemberById(event.getUser().getId()), event.getGuild().getRoleById(role.getRoleId())).queue(success -> {
                        event.getMember().getUser().openPrivateChannel().queue(privateChannel -> {
                            privateChannel.sendMessage("Removed Role: " + event.getGuild().getRoleById(role.getRoleId()).getName()).queue();
                        });
                    });
                    Main.embedRoleLog("Unassigned Role", event.getMember(), event.getGuild().getRoleById(role.getRoleId()));
                    Main.log("Unassigned role: \"" + event.getGuild().getRoleById(role.getRoleId()).getName() + "\" from: \"" + event.getMember().getEffectiveName() + "\".");
                }
            }
        }
    }

}
