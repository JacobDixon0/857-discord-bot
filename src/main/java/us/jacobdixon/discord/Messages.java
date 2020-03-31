package us.jacobdixon.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import us.jacobdixon.discord.configs.GlobalConfig;
import us.jacobdixon.discord.email.Email;
import us.jacobdixon.utils.Logger;
import us.jacobdixon.utils.StringToolbox;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.regex.Pattern;

public interface Messages {

    String DEFAULT_ICON = "https://www.jacobdixon.us/cache/static/discord-icon.png";
    String GMAIL_ICON = "https://www.jacobdixon.us/cache/static/gmail-icon.png";
    String ERROR_ICON = "https://www.jacobdixon.us/cache/static/error-icon.png";

    String BOT_EVENT = "Bot Event";
    String GUILD_EVENT = "Guild Event";
    String MOD_EVENT = "Mod Event";
    String ERROR_EVENT = "Error Response";
    String CONFIRMATION_EVENT = "Action Confirmation";

    int RED = 16711680;
    int BLUE = 255;
    int LIGHT_BLUE = 4490479;
    int GREEN = 65280;

    static Message getEmailNotificationMessage(Email email, AdvancedGuild guild, ArrayList<String> cachedAttachmentURLs) {
        MessageBuilder mb = new MessageBuilder();
        EmbedBuilder eb = buildEmbedBuilder(email.getSubject(), RED, "Sent to " + email.getDestinationUsers().get(0).getAddress(), GMAIL_ICON);

        String subjectSummary;

        if (email.getSubject().length() > 64) {
            subjectSummary = email.getSubject().substring(0, 64).trim() + "...";
        } else {
            subjectSummary = email.getSubject();
        }

        eb.setAuthor(email.getOriginUser().getIdentity(), "https://mail.google.com", email.getOriginUser().getProfileImageURL());
        eb.setThumbnail(guild.getGuild().getIconUrl());

        String content = email.getPlaintextContent();

        for (String filter : Main.globalConf.getEmailFilters()) {
            content = Pattern.compile(filter, Pattern.DOTALL).matcher(content).replaceAll("");
        }

        if (email.getPlaintextContent().length() > 1024) {
            ArrayList<String> contentSections = StringToolbox.splitGroups(content, 1024);

            eb.addField("Email Body:", contentSections.get(0), false);

            for (String contentSection : contentSections) {
                eb.addField("...", contentSection, false);
            }

        } else {
            eb.addField("Email Body:", content, false);
        }

        if (!cachedAttachmentURLs.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            boolean foundImage = false;
            for (String url : cachedAttachmentURLs) {
                sb.append(url).append("\n");

                if (!foundImage && url.matches(".+\\.(?:png|jpg|gif|webp|heic|svg|ico)$")) {
                    eb.setImage(url);
                    foundImage = true;
                }
            }
            eb.addField("Attached:", sb.toString(), false);
        }

        eb.setFooter("Sent to " + email.getDestinationUsers().get(0).getAddress(), GMAIL_ICON);

        try {
            eb.setTimestamp(Instant.ofEpochSecond(email.getDateEpoch()));
        } catch (ParseException e) {
            Main.logger.log(Logger.LogPriority.WARNING, "Could not parse email date for embed timestamp");
            eb.setTimestamp(Instant.now());
        }

        mb.setContent(guild.getAnnouncementsRole().getAsMention() + " Email announcement posted for **" + guild.getGuild().getName() + "**: *" + subjectSummary + "*");
        mb.setEmbed(eb.build());

        return mb.build();
    }

    static MessageEmbed getGenericAnnouncement(String author, String url, String authorIconURL, String title, String subtitle, String content, int color) {
        EmbedBuilder eb = buildEmbedBuilder(title, color, "Announcement", DEFAULT_ICON);
        eb.setAuthor(author, url, authorIconURL);
        eb.addField(subtitle, content, false);
        return eb.build();
    }

    static MessageEmbed getErrorResponse(String errorName, String errorNameShort, String errorDesc, int errorCode) {
        EmbedBuilder eb = buildEmbedBuilder("Error - " + errorName, RED, ERROR_EVENT, ERROR_ICON);
        eb.addField(new MessageEmbed.Field("Error Name", errorNameShort, true));
        eb.addField(new MessageEmbed.Field("Error Code", String.valueOf(errorCode), true));
        if (errorDesc != null) eb.setDescription(errorDesc);
        return eb.build();
    }

    static MessageEmbed getRoleAddedResponse(Role role, Guild guild) {
        EmbedBuilder eb = buildEmbedBuilder("Successfully Assigned Role", GREEN, CONFIRMATION_EVENT, DEFAULT_ICON);
        eb.addField(new MessageEmbed.Field("Role", role.getName(), true));
        eb.addField(new MessageEmbed.Field("Guild", guild.getName(), true));
        return eb.build();
    }

    static MessageEmbed getRoleRemovedResponse(Role role, Guild guild) {
        EmbedBuilder eb = buildEmbedBuilder("Successfully Removed Role", GREEN, CONFIRMATION_EVENT, DEFAULT_ICON);
        eb.addField(new MessageEmbed.Field("Role", role.getName(), true));
        eb.addField(new MessageEmbed.Field("Guild", guild.getName(), true));
        return eb.build();
    }

    static MessageEmbed getEmailAnnouncedLog(Email email, TextChannel channel) {
        EmbedBuilder eb = buildEmbedBuilder("Posted Email Announcement", GREEN, BOT_EVENT, DEFAULT_ICON);
        eb.addField("Sender", email.getOrigin(), false);
        eb.addField("Subject", email.getSubject(), false);
        eb.addField("Channel", channel.getAsMention(), false);
        eb.setThumbnail(email.getOriginUser().getProfileImageURL());
        return eb.build();
    }

    static MessageEmbed getRoleAddedLog(Member member, Role role) {
        EmbedBuilder eb = buildEmbedBuilder("Assigned Role", GREEN, MOD_EVENT, DEFAULT_ICON);
        eb.addField(new MessageEmbed.Field("Member", member.getAsMention(), true));
        eb.addField(new MessageEmbed.Field("Role", role.getAsMention(), true));
        return eb.build();
    }

    static MessageEmbed getRoleRemovedLog(Member member, Role role) {
        EmbedBuilder eb = buildEmbedBuilder("Unassigned Role", GREEN, MOD_EVENT, DEFAULT_ICON);
        eb.addField(new MessageEmbed.Field("Member", member.getAsMention(), true));
        eb.addField(new MessageEmbed.Field("Role", role.getAsMention(), true));
        return eb.build();
    }

    static MessageEmbed getClearedMessagesLog(TextChannel channel, Member initiator, int number) {
        EmbedBuilder eb = buildEmbedBuilder("Cleared " + number + " message(s)", RED, MOD_EVENT, DEFAULT_ICON);
        eb.addField("Channel", channel.getAsMention(), true);
        eb.addField("Authorization", initiator.getAsMention(), true);
        return eb.build();
    }

    static MessageEmbed getMemberJoinedLog(Member member) {
        EmbedBuilder eb = buildEmbedBuilder("Member Joined", GREEN, GUILD_EVENT, DEFAULT_ICON);
        eb.setThumbnail(member.getUser().getAvatarUrl());
        eb.setDescription(member.getAsMention());
        return eb.build();
    }

    static MessageEmbed getMemberLeftLog(User user) {
        EmbedBuilder eb = buildEmbedBuilder("Member Left", GREEN, GUILD_EVENT, DEFAULT_ICON);
        eb.setThumbnail(user.getAvatarUrl());
        eb.setDescription(user.getAsMention());
        return eb.build();
    }

    static MessageEmbed getBotStartedLog() {
        EmbedBuilder eb = buildEmbedBuilder("Bot Initiated", BLUE, BOT_EVENT, DEFAULT_ICON);
        eb.setDescription("Bot started on host `" + GlobalConfig.getHostname() + "`");
        return eb.build();
    }

    static EmbedBuilder buildEmbedBuilder(String title, int color, String footerText, String footerIconURL) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title);
        eb.setColor(color);
        eb.setFooter(footerText, footerIconURL);
        eb.setTimestamp(Instant.now());
        return eb;
    }
}
