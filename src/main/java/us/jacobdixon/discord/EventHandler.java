/*
 * Author: jd@jacobdixon.us (Jacob R. Dixon)
 * Date: 2020-03-31
 * Project: 857-discord-bot
 * Version: 1.1a
 * Repo: https://github.com/JacobDixon0/857-discord-bot
 */

package us.jacobdixon.discord;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.channel.priv.PrivateChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import us.jacobdixon.utils.Logger;
import us.jacobdixon.utils.StringToolbox;

import javax.annotation.Nonnull;
import java.util.Objects;

public class EventHandler extends ListenerAdapter {

    private Logger logger;
    private Database database;

    public EventHandler(Database database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    @Override
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        database.build(event.getJDA().getGuilds());
    }

    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        database.build(event.getJDA().getGuilds());
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
    }

    @Override
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent event) {
    }

    @Override
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
    }

    @Override
    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        AdvancedGuild guild = database.getAdvancedGuild(event.getGuild());

        if (guild != null) {
            guild.getLogChannel().sendMessage(Messages.getMemberJoinedLog(event.getMember())).queue();
        } else {
            logger.log(Logger.LogPriority.ERROR, "Could not find guild config for guild " + event.getGuild().getId() + " \"" + event.getGuild().getName() + "\"");
        }
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        AdvancedGuild guild = database.getAdvancedGuild(event.getGuild());

        if (guild != null) {
            guild.getLogChannel().sendMessage(Messages.getMemberLeftLog(event.getUser())).queue();
        } else {
            logger.log(Logger.LogPriority.ERROR, "Could not find guild config for guild " + event.getGuild().getId() + " \"" + event.getGuild().getName() + "\"");
        }
    }

    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {
    }

    @Override
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            event.getAuthor().openPrivateChannel().queue(privateChannel -> {
                if (event.getMessage().getTimeCreated().toEpochSecond() - privateChannel.getHistory().retrievePast(2).complete().get(1).getTimeCreated().toEpochSecond() > 30) {
                    privateChannel.sendMessage("Hello. If you're looking for help, you can reply to this message and an admin will review your message when available. Otherwise you can contact *beluga#6796* or send an email to *jd@jacobdixon.us*.").queue();
                }
            });
        }
    }

    @Override
    public void onPrivateMessageUpdate(@Nonnull PrivateMessageUpdateEvent event) {
    }

    @Override
    public void onPrivateChannelDelete(@Nonnull PrivateChannelDeleteEvent event) {
    }

    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {
    }

    @Override
    public void onGenericGuildMessageReaction(@Nonnull GenericGuildMessageReactionEvent event) {
        AdvancedGuild guild = database.getAdvancedGuild(event.getGuild());

        if (guild != null) {
            if (event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                for (EmoteRoleAssignmentMessage roleAssignmentMessage : guild.getEmoteRoleAssignmentMessages()) {
                    if (roleAssignmentMessage.getMessageId().equals(event.getMessageId()) && event.getReactionEmote().isEmoji()) {
                        if (roleAssignmentMessage.getRoleEmotePairs().containsKey(StringToolbox.escape(event.getReactionEmote().getEmoji()))) {
                            Role targetRole = event.getGuild().getRoleById(roleAssignmentMessage.getRoleEmotePairs().get(StringToolbox.escape(event.getReactionEmote().getEmoji())));
                            if (event instanceof GuildMessageReactionAddEvent) {
                                event.getGuild().addRoleToMember(Objects.requireNonNull(event.getMember()), Objects.requireNonNull(targetRole)).queue(success -> Objects.requireNonNull(event.getUser()).openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(Messages.getRoleAddedResponse(targetRole, event.getGuild())).queue()));
                                guild.getLogChannel().sendMessage(Messages.getRoleAddedLog(event.getMember(), targetRole)).queue();
                                logger.log("bot.event.guild.member.roles.add Added role \"" + targetRole.getName() + "\" to member \"" + event.getMember().getEffectiveName() + "\" in guild \"" + event.getGuild().getName() + "\" via emote role assigner");
                            } else if (event instanceof GuildMessageReactionRemoveEvent) {
                                event.getGuild().removeRoleFromMember(Objects.requireNonNull(event.getMember()), Objects.requireNonNull(targetRole)).queue(success -> Objects.requireNonNull(event.getUser()).openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(Messages.getRoleRemovedResponse(targetRole, event.getGuild())).queue()));
                                guild.getLogChannel().sendMessage(Messages.getRoleRemovedLog(event.getMember(), targetRole)).queue();
                                logger.log("bot.event.guild.member.roles.remove Added role \"" + targetRole.getName() + "\" to member \"" + event.getMember().getEffectiveName() + "\" in guild \"" + event.getGuild().getName() + "\" via emote role assigner");
                            }
                        }
                    }
                }
            } else {
                logger.log(Logger.LogPriority.WARNING, "Could not remove role for guild " + event.getGuild().getId() + " \"" + event.getGuild().getName() + "\" due to insufficient permissions");
            }
        } else {
            logger.log(Logger.LogPriority.ERROR, "Could not find guild config for guild " + event.getGuild().getId() + " \"" + event.getGuild().getName() + "\"");
        }
    }
}
