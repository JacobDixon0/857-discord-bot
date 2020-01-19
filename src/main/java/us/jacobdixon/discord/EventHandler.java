/*
 * Name: 857-discord-bot
 * Date: 2020/1/11
 * Author(s): jd@jacobdixon.us (Jacob Dixon)
 * Version: 1.0a
 */

package us.jacobdixon.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import us.jacobdixon.utils.StringFormatting;

public class EventHandler extends ListenerAdapter {

    private class SimpleReplace {
        String base;
        String[] replacements;

        public SimpleReplace(String base, String[] replacements) {
            this.base = base;
            this.replacements = replacements;
        }
    }

    private enum RemovalReason {
        MENTIONED_ROLE, CONTENT_POLICY, OTHER;
    }

    private SimpleReplace[] simpleReplacements = {
            new SimpleReplace("a", new String[]{"@", "4", "\u00E2", "\u00E3", "\u00E4", "\u00E5"}),
            new SimpleReplace("i", new String[]{"1", "!", "\u00EC", "\u00ED", "\u00EE", "\u00EF"}),
            new SimpleReplace("n", new String[]{"\u0144", "\u00F1"}),
            new SimpleReplace("s", new String[]{"5", "$"}),
            new SimpleReplace("t", new String[]{"7"}),
            new SimpleReplace("e", new String[]{"3"}),
            new SimpleReplace("o", new String[]{"0"}),
    };

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        Main.logger.log("Received private message from: \"" + event.getAuthor().getAsTag() + "\" : \"" + event.getMessage().getContentDisplay() + "\"");
        if (!event.getAuthor().getId().equals(Main.jda.getSelfUser().getId())) {
            Main.embedMessageLog(event.getAuthor(), event.getMessage().getContentDisplay());
            Main.logger.log("Received private message from \"" + event.getAuthor().getName() + "\" : \"" + event.getMessage().getContentDisplay() + "\"");
            event.getAuthor().openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage("This bot is not setup to handle private messages. If you have an issue please contact the admin beluga#6796, or send an email to discord@jacobdixon.us.").queue();
            });
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getMessage().getEmbeds().size() != 0) {
            Main.logger.log("Received message from: \"" + event.getAuthor().getAsTag() + "\" in: \"" + event.getGuild().getName() + "\"::\"" + event.getMessage().getTextChannel().getName() + "\" : \"" + event.getMessage().getContentDisplay() + "\" with: " + event.getMessage().getEmbeds().size() + " embeds");
        } else {
            Main.logger.log("Received message from: \"" + event.getAuthor().getAsTag() + "\" in: \"" + event.getGuild().getName() + "\"::\"" + event.getMessage().getTextChannel().getName() + "\" : \"" + event.getMessage().getContentDisplay() + "\"");
        }
        if (!event.getGuild().getMembersWithRoles(event.getGuild().getRoleById(Main.config.adminRoleId.getValue())).contains(event.getMember())
                && !event.getGuild().getMembersWithRoles(event.getGuild().getRoleById(Main.config.mentorRoleId.getValue())).contains(event.getMember())
                && !event.getAuthor().isBot()) {

            boolean allowed = true;
            Message message = event.getMessage();
            String messageContent = event.getMessage().getContentDisplay();
            RemovalReason reason = RemovalReason.OTHER;
            String violation = "";

            if (Main.config.useFilter.getValue()) {
                for (String s : Main.config.bannedPhrases.getValue()) {
                    String phrase = s;
                    s = "(?:.*)(?:^|\\s+)" + StringFormatting.formatRegex(s.toLowerCase()) + "(?:$|\\s+)(?:.*)";
                    if (messageContent.toLowerCase().matches(s) ||
                            messageContent.toLowerCase().matches(s + "s") ||
                            messageContent.toLowerCase().matches(s + "es")) {
                        allowed = false;
                        reason = RemovalReason.CONTENT_POLICY;
                        violation = phrase;
                        break;
                    }

                    for (SimpleReplace simpleReplacement : simpleReplacements) {
                        for (String replacement : simpleReplacement.replacements) {
                            if (messageContent.toLowerCase().replaceAll(StringFormatting.formatRegex(simpleReplacement.base), replacement).matches(s.replaceAll(simpleReplacement.base, replacement))) {
                                allowed = false;
                                reason = RemovalReason.CONTENT_POLICY;
                                violation = phrase.replaceAll(simpleReplacement.base, replacement);
                                break;
                            }
                        }
                    }
                }
            }

            if (allowed) {
                for (String is : Main.config.restrictedMentions.getValue()) {
                    for (Role role : message.getMentionedRoles()) {
                        if (role.getId().equals(is)) {
                            allowed = false;
                            reason = RemovalReason.MENTIONED_ROLE;
                            violation = "@" + role.getName();
                            break;
                        }
                    }
                }
            }

            if (!allowed) {
                event.getMessage().delete().queue();
                String finalViolatingPhrase = violation;
                RemovalReason finalReason = reason;
                event.getAuthor().openPrivateChannel().queue(privateChannel -> {
                    if (finalReason == RemovalReason.CONTENT_POLICY) {
                        privateChannel.sendMessage("Your message: \"" + messageContent.replaceAll(finalViolatingPhrase, "`" + finalViolatingPhrase + "`") + "\" was automatically removed for violating the guild's content policy. If this was a mistake, please contact an admin.").queue();

                    } else if (finalReason == RemovalReason.MENTIONED_ROLE) {
                        privateChannel.sendMessage("Your message: \"" + messageContent.replaceAll(finalViolatingPhrase, "`" + finalViolatingPhrase + "`") + "\" was automatically removed because you mentioned a role that has restricted mentioning. If this was a mistake, please contact an admin.").queue();
                    }
                });
                Main.embedFilterLog(event.getMember(), event.getChannel(), messageContent, reason.name(), violation);
            }
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Main.logger.log("Member joined: \"" + event.getMember().getUser().getAsTag() + "\"");
        if (event.getGuild().getId().equals(Main.config.serverId.getValue())) {
            if (event.getGuild().getId().equals(Main.config.serverId.getValue())) {
                event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(Main.config.memberRoleId.getValue())).queue();
            }
        }
        Main.embedMemberLog("Member Joined", event.getMember());
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        Main.logger.log("Member left: \"" + event.getMember().getUser().getAsTag() + "\"");
        Main.embedMemberLog("Member Left", event.getMember());
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        for (RoleAssigner role : Main.config.roleAssigners.getValue()) {
            if (event.getMessageId().equals(Main.config.roleAssignmentMessageId.getValue()) && event.getReactionEmote().getEmoji().equals(role.getEmote())) {
                if (event.getGuild().getId().equals(Main.config.serverId.getValue())) {
                    event.getGuild().addRoleToMember(event.getGuild().getMemberById(event.getUser().getId()), event.getGuild().getRoleById(role.getRoleId())).queue(success -> {
                        event.getMember().getUser().openPrivateChannel().queue(privateChannel -> {
                            privateChannel.sendMessage("Assigned Role: " + event.getGuild().getRoleById(role.getRoleId()).getName()).queue();
                        });
                    });
                    Main.logger.log("Assigned role: \"" + event.getGuild().getRoleById(role.getRoleId()).getName() + "\" to: \"" + event.getMember().getEffectiveName() + "\"");
                    Main.embedRoleLog("Assigned Role", event.getMember(), event.getGuild().getRoleById(role.getRoleId()));
                }
            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
        for (RoleAssigner role : Main.config.roleAssigners.getValue()) {
            if (event.getMessageId().equals(Main.config.roleAssignmentMessageId.getValue()) && event.getReactionEmote().getEmoji().equals(role.getEmote())) {
                if (event.getGuild().getId().equals(Main.config.serverId.getValue())) {
                    event.getGuild().removeRoleFromMember(event.getGuild().getMemberById(event.getUser().getId()), event.getGuild().getRoleById(role.getRoleId())).queue(success -> {
                        event.getMember().getUser().openPrivateChannel().queue(privateChannel -> {
                            privateChannel.sendMessage("Removed Role: " + event.getGuild().getRoleById(role.getRoleId()).getName()).queue();
                        });
                    });
                    Main.logger.log("Unassigned role: \"" + event.getGuild().getRoleById(role.getRoleId()).getName() + "\" from: \"" + event.getMember().getEffectiveName() + "\"");
                    Main.embedRoleLog("Unassigned Role", event.getMember(), event.getGuild().getRoleById(role.getRoleId()));
                }
            }
        }
    }

}
