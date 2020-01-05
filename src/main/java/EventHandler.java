import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class EventHandler extends ListenerAdapter {

    class RoleAssigner{
        String messageId;
        String roleId;
        String emote;

        public RoleAssigner(String messageId, String roleId, String emote) {
            this.messageId = messageId;
            this.roleId = roleId;
            this.emote = emote;
        }
    }

    private RoleAssigner[] roles = new RoleAssigner[]{
      new RoleAssigner(Main.roleMessageId, "663138202038566924", "\u2757"),
            new RoleAssigner(Main.roleMessageId, "663133525595127839", "\uD83D\uDCBB"),
            new RoleAssigner(Main.roleMessageId, "663133581475971082", "\uD83D\uDCF7"),
            new RoleAssigner(Main.roleMessageId, "663133374097129483", "\uD83E\uDD16"),
            new RoleAssigner(Main.roleMessageId, "663133612954222632", "\uD83C\uDFAE")
    };

    public void resetRoleAssigner(){
        roles = new RoleAssigner[]{
                new RoleAssigner(Main.roleMessageId, "663138202038566924", "\u2757"),
                new RoleAssigner(Main.roleMessageId, "663133525595127839", "\uD83D\uDCBB"),
                new RoleAssigner(Main.roleMessageId, "663133581475971082", "\uD83D\uDCF7"),
                new RoleAssigner(Main.roleMessageId, "663133374097129483", "\uD83E\uDD16"),
                new RoleAssigner(Main.roleMessageId, "663133612954222632", "\uD83C\uDFAE")
        };
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        Main.embedMessageLog(event.getAuthor(), event.getMessage().getContentDisplay());
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Main.embedMemberLog("Member Joined", event.getMember());
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        Main.embedMemberLog("Member Left", event.getMember());
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        for(RoleAssigner role : roles){
            if(event.getMessageId().equals(role.messageId) && event.getReactionEmote().getName().equals(role.emote)){
                event.getGuild().getController().addRolesToMember(event.getGuild().getMemberById(event.getUser().getId()), event.getGuild().getRoleById(role.roleId)).queue(success -> {
                    event.getMember().getUser().openPrivateChannel().queue(privateChannel -> {
                        privateChannel.sendMessage("Assigned Role: " + event.getGuild().getRoleById(role.roleId).getName()).queue();
                    });
                });
                Main.embedRoleLog("Assigned Role", event.getMember(), event.getGuild().getRoleById(role.roleId));
            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
        for(RoleAssigner role : roles){
            if(event.getMessageId().equals(role.messageId) && event.getReactionEmote().getName().equals(role.emote)){
                event.getGuild().getController().removeRolesFromMember(event.getGuild().getMemberById(event.getUser().getId()), event.getGuild().getRoleById(role.roleId)).queue(success -> {
                    event.getMember().getUser().openPrivateChannel().queue(privateChannel -> {
                        privateChannel.sendMessage("Removed Role: " + event.getGuild().getRoleById(role.roleId).getName()).queue();
                    });
                });
                Main.embedRoleLog("Unassigned Role", event.getMember(), event.getGuild().getRoleById(role.roleId));
            }
        }
    }

}
