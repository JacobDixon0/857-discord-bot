/*
 * Name: 857-discord-bot
 * Date: 2020/1/11
 * Author(s): jd@jacobdixon.us (Jacob Dixon)
 * Version: 1.0a
 */

package us.jacobdioxn.discord;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventHandler extends ListenerAdapter {

    private class SimpleReplace {
        String base;
        String[] replacements;

        public SimpleReplace(String base, String[] replacements) {
            this.base = base;
            this.replacements = replacements;
        }
    }

    private SimpleReplace[] simpleReplacements = {
            new SimpleReplace("a", new String[]{"@", "4", "\u00E2", "\u00E3", "\u00E4", "\u00E5"}),
            new SimpleReplace("i", new String[]{"1", "!", "\u00EC", "\u00ED", "\u00EE", "\u00EF"}),
            new SimpleReplace("n", new String[]{"\u0144", "\u00F1"}),
            new SimpleReplace("s", new String[]{"5", "\\$"}),
            new SimpleReplace("t", new String[]{"7"}),
            new SimpleReplace("e", new String[]{"3"}),
            new SimpleReplace("o", new String[]{"0"}),
    };

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (!event.getAuthor().getId().equals(Main.jda.getSelfUser().getId())) {
            Main.embedMessageLog(event.getAuthor(), event.getMessage().getContentDisplay());
            Main.log("Received private message from \"" + event.getAuthor().getName() + "\" : \"" + event.getMessage().getContentDisplay() + "\".");
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (!event.getGuild().getMembersWithRoles(event.getGuild().getRoleById(Main.adminRoleId)).contains(event.getMember()) && !event.getAuthor().isBot()) {

            boolean allowed = true;

            String message = event.getMessage().getContentDisplay();
            String violatingPhrase = "";

            for (String s : Main.bannedPhrases) {
                if (message.toLowerCase().contains(s.toLowerCase()) || message.toLowerCase().contains(s.toLowerCase() + "s") || message.toLowerCase().contains(s.toLowerCase() + "es")) {
                    allowed = false;
                    violatingPhrase = s;
                    break;
                }

                for (SimpleReplace simpleReplacement : simpleReplacements) {
                    for (String replacement : simpleReplacement.replacements) {
                        if (message.toLowerCase().replaceAll(simpleReplacement.base, replacement).contains(s.toLowerCase().replaceAll(simpleReplacement.base, replacement))) {
                            allowed = false;
                            violatingPhrase = s.replaceAll(simpleReplacement.base, replacement);
                            break;
                        }
                    }
                }
            }

            if (!allowed) {
                event.getMessage().delete().queue();
                String finalViolatingPhrase = violatingPhrase;
                String finalViolatingPhrase1 = violatingPhrase;
                event.getAuthor().openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage("Your message: \"" + message.replaceAll(finalViolatingPhrase, "`" + finalViolatingPhrase1 + "`") + "\" was automatically removed for violating the guild's content policy. If this was a mistake, please contact an admin.").queue();
                });
                Main.embedFilterLog(event.getMember(), event.getChannel(), message, violatingPhrase);
            }
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (event.getGuild().getId().equals(Main.serverId)) {
            if (event.getGuild().getId().equals(Main.serverId)) {
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
        for (RoleAssigner role : Main.roleAssigners) {
            if (event.getMessageId().equals(Main.roleAssignmentMessageId) && event.getReactionEmote().getEmoji().equals(role.getEmote())) {
                if (event.getGuild().getId().equals(Main.serverId)) {
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
        for (RoleAssigner role : Main.roleAssigners) {
            if (event.getMessageId().equals(Main.roleAssignmentMessageId) && event.getReactionEmote().getEmoji().equals(role.getEmote())) {
                if (event.getGuild().getId().equals(Main.serverId)) {
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
