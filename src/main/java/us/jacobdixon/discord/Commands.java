/*
 * Author: jd@jacobdixon.us (Jacob R. Dixon)
 * Date: 2020-03-31
 * Project: 857-discord-bot
 * Version: 1.1a
 * Repo: https://github.com/JacobDixon0/857-discord-bot
 */

package us.jacobdixon.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import org.json.simple.parser.ParseException;
import us.jacobdixon.discord.configs.ConfigEntry;
import us.jacobdixon.discord.email.EmailUser;
import us.jacobdixon.discord.exceptions.ConfigException;
import us.jacobdixon.discord.exceptions.InvalidConfigException;
import us.jacobdixon.discord.exceptions.MissingConfigException;
import us.jacobdixon.utils.StringToolbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ExecutionException;

public interface Commands extends ReturnCodes {

    class PingCommand extends Command {

        PingCommand() {
            this.name = "ping";
            this.aliases = new String[]{"p"};
        }

        @Override
        protected void execute(CommandEvent event) {
            event.getMessage().addReaction("\uD83C\uDFD3").queue();
        }
    }

    class EchoCommand extends Command {

        EchoCommand() {
            this.name = "echo";
            this.aliases = new String[]{"say"};
            this.guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            int status;

            if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                if (!event.getArgs().equals("")) {
                    if (!event.getMessage().getMentionedChannels().isEmpty()) {
                        event.getMessage().getMentionedChannels().get(0).sendMessage(event.getArgs().replaceFirst(event.getMessage().getMentionedChannels().get(0).getAsMention(), "")).queue();
                    } else {
                        event.reply(event.getArgs());
                    }
                    status = OKAY;
                } else {
                    status = MISSING_ARGS;
                }
            } else {
                status = INSUFFICIENT_PERMISSIONS;
            }

            ReturnCodes.handleCommand(status, event);
        }
    }

    class EchoEditCommand extends Command {

        EchoEditCommand() {
            this.name = "echoe";
            this.aliases = new String[]{"edit", "eecho"};
            this.guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            int status;

            if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                String[] argsList = event.getArgs().split(" ");
                if (argsList.length >= 2) {
                    try {
                        if (!event.getMessage().getMentionedChannels().isEmpty()) {
                            event.getMessage().getMentionedChannels().get(0).editMessageById(argsList[1], event.getArgs().replaceFirst(argsList[0] + " " + argsList[1], "")).queue();
                        } else {
                            event.getTextChannel().editMessageById(argsList[0], event.getArgs().replaceFirst(argsList[0] + " ", "")).queue();
                        }
                        status = OKAY;
                    } catch (IllegalArgumentException e) {
                        status = INVALID_ARGS;
                    }
                } else {
                    status = MISSING_ARGS;
                }
            } else {
                status = INSUFFICIENT_PERMISSIONS;
            }

            ReturnCodes.handleCommand(status, event);
        }
    }

    class EchoFileCommand extends Command {

        EchoFileCommand() {
            this.name = "echof";
            this.aliases = new String[]{"fecho"};
        }

        @Override
        public void execute(CommandEvent event) {
            int status;

            if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                AdvancedGuild guild = Main.db.getAdvancedGuild(event.getGuild());
                if (guild == null) {
                    status = INTERNAL_SERVER_ERROR;
                } else {
                    if (!event.getMessage().getAttachments().isEmpty()) {
                        try {
                            File uploadFile = Main.db.tempCacheMessageAttachmentsGuild(event.getMessage()).get(0);

                            if (!event.getMessage().getMentionedChannels().isEmpty()) {
                                event.getMessage().getMentionedChannels().get(0).sendFile(uploadFile).queue();
                            } else {
                                event.reply(uploadFile, uploadFile.getName());
                            }
                            status = 0;
                        } catch (InterruptedException | ExecutionException e) {
                            Main.logger.log(e, "Exception caught uploading file to guild temp cache directory");
                            status = INTERNAL_SERVER_ERROR;
                        }
                    } else {
                        status = MISSING_FILE_UPLOAD;
                    }
                }
            } else {
                status = INSUFFICIENT_PERMISSIONS;
            }
            ReturnCodes.handleCommand(status, event);
        }
    }

    class PurgeCommand extends Command {

        PurgeCommand() {
            this.name = "purge";
            this.aliases = new String[]{"pg", "clear"};
            this.guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {

            if (event.getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                if (event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                    try {
                        int messageHistoryAmount = Integer.parseInt(event.getArgs()) + 1;
                        event.getChannel().getHistory().retrievePast(messageHistoryAmount).complete(true).forEach(e -> e.delete().queue());
                        Main.db.getAdvancedGuild(event.getGuild()).getLogChannel().sendMessage(Messages.getClearedMessagesLog(event.getTextChannel(), event.getMember(), messageHistoryAmount)).queue();
                    } catch (RateLimitedException e) {
                        Main.logger.log(e, "Exception caught clearing channel history");
                    } catch (NumberFormatException e) {
                        ReturnCodes.handleCommand(INVALID_ARGS, event);
                    }
                } else {
                    ReturnCodes.handleCommand(INSUFFICIENT_PERMISSIONS, event);
                }
            } else {
                ReturnCodes.handleCommand(INSUFFICIENT_BOT_PERMISSIONS, event);
            }
        }
    }

    class ManualEventAnnounceCommand extends Command {

        ManualEventAnnounceCommand() {
            this.name = "eannounce";
        }

        @Override
        protected void execute(CommandEvent event) {
            int status = -1;

            if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                String[] argsList = StringToolbox.split(event.getArgs(), Main.globalConf.getDelimiter());

                if (!event.getMessage().getMentionedChannels().isEmpty() && argsList.length == 4 || argsList.length == 5) {
                    TextChannel targetChannel = event.getMessage().getMentionedChannels().get(0);

                    String iconURL = "https://www.jacobdixon.us/cache/static/calendar-icon.png";
                    if (argsList.length == 5 && !argsList[4].equals("x") && !argsList[4].equals("")) {
                        iconURL = argsList[4];
                    }

                    targetChannel.sendMessage(Messages.getGenericAnnouncement(argsList[1], "https://calander.google.com",
                            iconURL, argsList[2], "Event", argsList[3], Messages.LIGHT_BLUE)).queue();

                } else {
                    status = MISSING_ARGS;
                }
            } else {
                status = INSUFFICIENT_PERMISSIONS;
            }
            ReturnCodes.handleCommand(status, event);
        }
    }

    class ConfigCommand extends Command {

        ConfigCommand() {
            this.name = "config";
            this.aliases = new String[]{"conf", "con"};
            this.guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            int status;

            String[] argsList = event.getArgs().split(" ");
            AdvancedGuild guild = Main.db.getAdvancedGuild(event.getGuild());

            if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                if (guild == null) {
                    status = INTERNAL_SERVER_ERROR;
                } else {
                    if (argsList[0] != null) {
                        switch (argsList[0]) {
                            case "save":
                                try {
                                    guild.getConfig().save();
                                    status = OKAY;
                                } catch (FileNotFoundException e) {
                                    Main.logger.log(e, "Exception caught saving configs via command for guild " + event.getGuild().getId() + " \"" + event.getGuild().getName() + "\", file not found");
                                    status = INTERNAL_SERVER_ERROR;
                                } catch (MissingConfigException e) {
                                    Main.logger.log(e, "Exception caught saving configs via command for guild " + event.getGuild().getId() + " \"" + event.getGuild().getName() + "\", config not loaded");
                                    status = INTERNAL_SERVER_ERROR;
                                }
                                break;
                            case "load":
                                try {
                                    guild.getConfig().load();
                                    status = OKAY;
                                } catch (ParseException | IOException e) {
                                    Main.logger.log(e, "Exception caught loading configs via command for guild " + event.getGuild().getId() + " \"" + event.getGuild().getName() + "\"");
                                    status = INTERNAL_SERVER_ERROR;
                                } catch (MissingConfigException e) {
                                    Main.logger.log(e, "Exception caught saving configs via command for guild " + event.getGuild().getId() + " \"" + event.getGuild().getName() + "\", config not loaded");
                                    status = INTERNAL_SERVER_ERROR;
                                } catch (InvalidConfigException e) {
                                    Main.logger.log(e, "Exception caught saving configs via command for guild " + event.getGuild().getId() + " \"" + event.getGuild().getName() + "\", config not loaded");
                                    status = INVALID_CONFIG;
                                }
                                break;
                            case "get":
                                if (argsList.length > 1) {
                                    if (guild.getConfig().getConfig(argsList[1]) != null) {
                                        event.reply(event.getAuthor().getAsMention() + " **Key: ** `" + argsList[1] + "` : **Value:** `" + guild.getConfig().getConfig(argsList[2]) + "`");
                                        status = OKAY;
                                    } else {
                                        status = INVALID_ARGS;
                                    }
                                } else {
                                    StringBuilder sb = new StringBuilder(event.getAuthor().getAsMention() + "\n**All Guild Configs**\n");
                                    for (ConfigEntry<?> configEntry : guild.getConfig().getConfigs()) {
                                        sb.append("**Key: ** `").append(configEntry.getKey()).append("` : **Value:** `").append(configEntry.getValue()).append("`");
                                    }
                                    event.reply(sb.toString());
                                    status = OKAY;
                                }
                                break;
                            case "email":
                                switch (argsList[1]) {
                                    case ("origins"):
                                        if (argsList[2].equals("list")) {
                                            StringBuilder sb = new StringBuilder("**Whitelisted Email Origins**\n");
                                            for (EmailUser emailUser : guild.getWhitelistedEmailOrigins()) {
                                                sb.append(emailUser.getIdentity()).append("\n");
                                            }
                                            event.reply(sb.toString());
                                            status = OKAY;
                                        } else {
                                            status = INVALID_COMMAND;
                                        }
                                        break;
                                    case ("destinations"):
                                        if (argsList[2].equals("list")) {
                                            StringBuilder sb = new StringBuilder("**Whitelisted Email Destinations**\n");
                                            for (EmailUser emailUser : guild.getWhitelistedEmailOrigins()) {
                                                sb.append(emailUser.getIdentity()).append("\n");
                                            }
                                            event.reply(sb.toString());
                                            status = OKAY;
                                        } else {
                                            status = INVALID_COMMAND;
                                        }
                                        break;
                                    default:
                                        status = INVALID_COMMAND;
                                }
                                break;
                            case "upload":
                                if (!event.getMessage().getAttachments().isEmpty()) {
                                    try {
                                        File configUploadFile = Main.db.tempCacheMessageAttachmentsGuild(event.getMessage()).get(0);
                                        status = Main.db.userUploadGuildConfig(guild, configUploadFile);
                                    } catch (ExecutionException | InterruptedException e) {
                                        Main.logger.log(e, "Exception caught uploading guild config");
                                        status = INTERNAL_SERVER_ERROR;
                                    }
                                } else {
                                    status = MISSING_FILE_UPLOAD;
                                }
                                break;
                            case "download":
                                event.reply(guild.getLoadedConfigFile(), guild.getLoadedConfigFile().getName());
                                status = OKAY;
                                break;
                            default:
                                status = INVALID_COMMAND;
                                break;
                        }
                    } else {
                        status = MISSING_ARGS;
                    }
                }
            } else {
                status = INSUFFICIENT_PERMISSIONS;
            }
            ReturnCodes.handleCommand(status, event);
        }
    }


    class AdminCommand extends Command {

        AdminCommand() {
            this.name = "admin";
            this.aliases = new String[]{"a", "q"};
        }

        @Override
        protected void execute(CommandEvent event) {
            int status;
            String[] argsList = event.getArgs().split(" ");

            if (Main.globalConf.getAdminIDs().contains(event.getAuthor().getId())) {
                switch (argsList[0]) {
                    case "cc":
                        Main.db.clearGuildTempCache();
                        status = OKAY;
                        break;
                    case "mode":
                        try {
                            Main.setBotMode(Long.parseLong(argsList[1]));
                            status = OKAY;
                        } catch (NumberFormatException e) {
                            status = INVALID_ARGS;
                        }
                        break;
                    case "inbox":
                        try {
                            event.reply(event.getAuthor().getAsMention() + "\n" + Main.getEmailHandler().getRecentInboxSummary());
                            status = OKAY;
                        } catch (IOException e) {
                            status = INTERNAL_SERVER_ERROR;
                        }
                        break;
                    case "config":
                        switch (argsList[1]) {
                            case "global":
                                switch (argsList[2]) {
                                    case "load":
                                        try {
                                            Main.globalConf.load();
                                            Main.configureJDA();
                                            status = OKAY;
                                        } catch (IOException | ParseException | ConfigException e) {
                                            Main.logger.log(e, "Error loading global config");
                                            status = INTERNAL_SERVER_ERROR;
                                        }
                                        break;
                                    case "save":
                                        try {
                                            Main.globalConf.save();
                                            status = OKAY;
                                        } catch (IOException | MissingConfigException e) {
                                            Main.logger.log(e, "Error saving global config");
                                            status = INTERNAL_SERVER_ERROR;
                                        }
                                        break;
                                    case "upload":
                                        if (!event.getMessage().getAttachments().isEmpty()) {
                                            try {
                                                File configUpload = Main.db.tempCacheMessageAttachments(event.getMessage()).get(0);
                                                status = Main.db.userUploadGlobalConfig(configUpload);
                                            } catch (ExecutionException | InterruptedException e) {
                                                Main.logger.log(e, "Exception caught uploading guild config");
                                                status = INTERNAL_SERVER_ERROR;
                                            }
                                        } else {
                                            status = MISSING_FILE_UPLOAD;
                                        }
                                        break;
                                    case "download":
                                        event.reply(Main.globalConf.getLoadedConfigFile(), Main.globalConf.getLoadedConfigFile().getName());
                                        status = OKAY;
                                        break;
                                    case "get":
                                        if (Main.globalConf.getConfig(argsList[3]) != null) {
                                            event.reply(event.getAuthor().getAsMention() + " **Key: ** `" + argsList[1] + "` : **Value:** `" + Main.globalConf.getConfig(argsList[1]).getValue() + "`");
                                            status = OKAY;
                                        } else {
                                            status = INVALID_ARGS;
                                        }
                                        break;
                                    default:
                                        status = INVALID_COMMAND;
                                        break;
                                }
                                break;
                            case "guild":
                                if (Main.db.getAdvancedGuildById(argsList[2]) != null) {
                                    AdvancedGuild targetGuild = Main.db.getAdvancedGuildById(argsList[2]);

                                    switch (argsList[3]) {
                                        case "save":
                                            try {
                                                targetGuild.getConfig().save();
                                                status = OKAY;
                                            } catch (FileNotFoundException e) {
                                                Main.logger.log(e, "Exception caught saving configs via command for guild " + event.getGuild().getId() + " \"" + event.getGuild().getName() + "\", file not found");
                                                status = INTERNAL_SERVER_ERROR;
                                            } catch (MissingConfigException e) {
                                                Main.logger.log(e, "Exception caught saving configs via command for guild " + event.getGuild().getId() + " \"" + event.getGuild().getName() + "\", config not loaded");
                                                status = INTERNAL_SERVER_ERROR;
                                            }
                                            break;
                                        case "load":
                                            try {
                                                targetGuild.getConfig().load();
                                                status = OKAY;
                                            } catch (ParseException | IOException e) {
                                                Main.logger.log(e, "Exception caught loading configs via command for guild " + event.getGuild().getId() + " \"" + event.getGuild().getName() + "\"");
                                                status = INTERNAL_SERVER_ERROR;
                                            } catch (MissingConfigException e) {
                                                Main.logger.log(e, "Exception caught saving configs via command for guild " + event.getGuild().getId() + " \"" + event.getGuild().getName() + "\", config not loaded");
                                                status = INTERNAL_SERVER_ERROR;
                                            } catch (InvalidConfigException e) {
                                                Main.logger.log(e, "Exception caught saving configs via command for guild " + event.getGuild().getId() + " \"" + event.getGuild().getName() + "\", config not loaded");
                                                status = INVALID_CONFIG;
                                            }
                                            break;
                                        case "get":
                                            if (argsList.length > 4) {
                                                if (targetGuild.getConfig().getConfig(argsList[4]) != null) {
                                                    event.reply(event.getAuthor().getAsMention() + " **Key: ** `" + argsList[4] + "` : **Value:** `" + targetGuild.getConfig().getConfig(argsList[2]) + "`");
                                                    status = OKAY;
                                                } else {
                                                    status = INVALID_ARGS;
                                                }
                                            } else {
                                                StringBuilder sb = new StringBuilder(event.getAuthor().getAsMention() + "\n**All Guild Configs**\n");
                                                for (ConfigEntry<?> configEntry : targetGuild.getConfig().getConfigs()) {
                                                    sb.append("**Key: ** `").append(configEntry.getKey()).append("` : **Value:** `").append(configEntry.getValue()).append("`");
                                                }
                                                event.reply(sb.toString());
                                                status = OKAY;
                                            }
                                            break;
                                        case "email":
                                            switch (argsList[4]) {
                                                case ("origins"):
                                                    if (argsList[5].equals("list")) {
                                                        StringBuilder sb = new StringBuilder("**Whitelisted Email Origins**\n");
                                                        for (EmailUser emailUser : targetGuild.getWhitelistedEmailOrigins()) {
                                                            sb.append(emailUser.getIdentity()).append("\n");
                                                        }
                                                        event.reply(sb.toString());
                                                        status = OKAY;
                                                    } else {
                                                        status = INVALID_COMMAND;
                                                    }
                                                    break;
                                                case ("destinations"):
                                                    if (argsList[5].equals("list")) {
                                                        StringBuilder sb = new StringBuilder("**Whitelisted Email Destinations**\n");
                                                        for (EmailUser emailUser : targetGuild.getWhitelistedEmailOrigins()) {
                                                            sb.append(emailUser.getIdentity()).append("\n");
                                                        }
                                                        event.reply(sb.toString());
                                                        status = OKAY;
                                                    } else {
                                                        status = INVALID_COMMAND;
                                                    }
                                                    break;
                                                default:
                                                    status = INVALID_COMMAND;
                                            }
                                            break;
                                        case "upload":
                                            if (!event.getMessage().getAttachments().isEmpty()) {
                                                try {
                                                    File configUploadFile = Main.db.tempCacheMessageAttachmentsGuild(event.getMessage()).get(0);
                                                    status = Main.db.userUploadGuildConfig(targetGuild, configUploadFile);
                                                } catch (ExecutionException | InterruptedException e) {
                                                    Main.logger.log(e, "Exception caught uploading guild config");
                                                    status = INTERNAL_SERVER_ERROR;
                                                }
                                            } else {
                                                status = MISSING_FILE_UPLOAD;
                                            }
                                            break;
                                        case "download":
                                            event.reply(targetGuild.getLoadedConfigFile(), targetGuild.getLoadedConfigFile().getName());
                                            status = OKAY;
                                            break;
                                        default:
                                            status = INVALID_COMMAND;
                                            break;
                                    }
                                } else {
                                    status = INVALID_ARGS;
                                }
                                break;
                            default:
                                status = INVALID_COMMAND;
                        }
                        break;
                    case "uptime":
                        long uptime = (Instant.now().getEpochSecond() - Main.globalConf.INIT_TIME);
                        event.reply(event.getAuthor().getAsMention() + " **Uptime:** " + StringToolbox.formatTime(uptime));
                        status = OKAY;
                        break;
                    case "stop":
                        if (argsList.length > 1 && argsList[1].equals("now")) {
                            Main.exit(0);
                        } else {
                            Main.stop();
                        }
                        status = OKAY;
                        break;
                    default:
                        status = INVALID_COMMAND;
                }
                ReturnCodes.handleCommand(status, event);
            }
        }
    }
}
