/*
 * Author: jd@jacobdixon.us (Jacob R. Dixon)
 * Date: 2020-03-31
 * Project: 857-discord-bot
 * Version: 1.1a
 * Repo: https://github.com/JacobDixon0/857-discord-bot
 */

package us.jacobdixon.discord;

import net.dv8tion.jda.api.events.channel.priv.PrivateChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import us.jacobdixon.utils.Logger;

import javax.annotation.Nonnull;
import java.util.Objects;

public class EventLogger extends ListenerAdapter {

    private Logger logger;

    public EventLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        logger.log("guild.join \"" + event.getGuild().getName() + "\" (" + event.getGuild().getId() + ")");
    }

    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        logger.log("guild.leave \"" + event.getGuild().getName() + "\" (" + event.getGuild().getId() + ")");
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (event.getMessage().getAttachments().isEmpty()) {
            logger.log("guild.message.received \"" + event.getGuild().getName() + "\"::\"" + event.getChannel().getName() + "\"::\"" + event.getAuthor().getName() + "\": \"" + event.getMessage().getContentRaw() + "\" (" + event.getGuild().getId() + ":" + event.getChannel().getId() + ":" + event.getAuthor().getId() + ":" + event.getMessageId() + ")");
        } else {
            logger.log("guild.message.received \"" + event.getGuild().getName() + "\"::\"" + event.getChannel().getName() + "\"::\"" + event.getAuthor().getName() + "\": \"" + event.getMessage().getContentRaw() + "\" attachments: " + event.getMessage().getAttachments().size() + " (" + event.getGuild().getId() + ":" + event.getChannel().getId() + ":" + event.getAuthor().getId() + ":" + event.getMessageId() + ")");
        }
    }

    @Override
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent event) {
        logger.log("guild.message.update \"" + event.getGuild().getName() + "\"::\"" + event.getChannel().getName() + "\"::\"" + event.getAuthor().getName() + "\": \"" + event.getMessage().getContentRaw() + "\" (" + event.getGuild().getId() + ":" + event.getChannel().getId() + ":" + event.getAuthor().getId() + ":" + event.getMessageId() + ")");
    }

    @Override
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        logger.log("guild.message.delete \"" + event.getGuild().getName() + "\"::\"" + event.getChannel().getName() + "\" (" + event.getGuild().getId() + ":" + event.getChannel().getId() + ":" + event.getMessageId() + ")");
    }

    @Override
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if (event.getReactionEmote().isEmote()) {
            logger.log("guild.message.reactions.add \"" + event.getGuild().getName() + "\"::\"" + event.getChannel().getName() + "\"::\"" + event.getUser().getName() + "\": \"" + event.getReactionEmote().getName() + "\" (" + event.getGuild().getId() + ":" + event.getChannel().getId() + ":" + event.getUser().getId() + ":" + event.getReactionEmote().getId() + ")");
        } else {
            logger.log("guild.message.reactions.add \"" + event.getGuild().getName() + "\"::\"" + event.getChannel().getName() + "\"::\"" + event.getUser().getName() + "\": \"" + event.getReactionEmote().getName() + "\" (" + event.getGuild().getId() + ":" + event.getChannel().getId() + ":" + event.getUser().getId() + ":" + event.getReactionEmote().getEmoji() + ")");
        }
    }

    @Override
    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
        if (event.getReactionEmote().isEmote()) {
            logger.log("guild.message.reactions.remove \"" + event.getGuild().getName() + "\"::\"" + event.getChannel().getName() + "\"::\"" + Objects.requireNonNull(event.getUser()).getName() + "\": \"" + event.getReactionEmote().getName() + "\" (" + event.getGuild().getId() + ":" + event.getChannel().getId() + ":" + event.getUser().getId() + ":" + event.getReactionEmote().getId() + ")");
        } else {
            logger.log("guild.message.reactions.remove \"" + event.getGuild().getName() + "\"::\"" + event.getChannel().getName() + "\"::\"" + Objects.requireNonNull(event.getUser()).getName() + "\": \"" + event.getReactionEmote().getName() + "\" (" + event.getGuild().getId() + ":" + event.getChannel().getId() + ":" + event.getUser().getId() + ":" + event.getReactionEmote().getEmoji() + ")");
        }
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        logger.log("guild.member.join \"" + event.getGuild().getName() + "\":\"" + event.getUser().getName() + "\" (" + event.getGuild().getId() + ":" + event.getUser().getId() + ")");
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        logger.log("guild.member.remove \"" + event.getGuild().getName() + "\":\"" + event.getUser().getName() + "\" (" + event.getGuild().getId() + ":" + event.getUser().getId() + ")");
    }

    @Override
    public void onGuildMemberUpdateNickname(@Nonnull GuildMemberUpdateNicknameEvent event) {
        logger.log("guild.member.nickname \"" + event.getGuild().getName() + "\":\"" + event.getOldNickname() + "\": \"" + event.getNewNickname() + "\" (" + event.getGuild().getId() + ":" + event.getUser().getId() + ")");
    }

    @Override
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
        logger.log("dms.message.received \"" + event.getAuthor().getName() + "\": \"" + event.getMessage().getContentRaw() + "\" (" + event.getChannel().getId() + ":" + event.getAuthor().getId() + ":" + event.getMessageId() + ")");
    }

    @Override
    public void onPrivateMessageUpdate(@Nonnull PrivateMessageUpdateEvent event) {
        logger.log("dms.message.update \"" + event.getAuthor().getName() + "\": \"" + event.getMessage().getContentRaw() + "\" (" + event.getChannel().getId() + ":" + event.getAuthor().getId() + ":" + event.getMessageId() + ")");
    }

    @Override
    public void onPrivateChannelDelete(@Nonnull PrivateChannelDeleteEvent event) {
        logger.log("dms.message.delete \"" + event.getUser().getName() + "\" (" + event.getChannel().getId() + ":" + event.getUser().getId() + ")");
    }

    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {
        logger.log("guild.member.roles.add \"" + event.getGuild().getName() +
                "\"::\"" + event.getMember().getEffectiveName() +
                "\": \"" + event.getRoles().get(0).getName() +
                "\" (" + event.getGuild().getId() +
                ":" + event.getMember().getId() +
                ":" + event.getRoles().get(0).getId() +
                ")");
    }

    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {
        logger.log("guild.member.roles.add \"" + event.getGuild().getName() +
                "\"::\"" + event.getMember().getEffectiveName() +
                "\": \"" + event.getRoles().get(0).getName() +
                "\" (" + event.getGuild().getId() +
                ":" + event.getMember().getId() +
                ":" + event.getRoles().get(0).getId() +
                ")");
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
