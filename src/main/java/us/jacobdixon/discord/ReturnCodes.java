package us.jacobdixon.discord;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

interface ReturnCodes {
    int OKAY = 0;
    int INTERNAL_SERVER_ERROR = 1;
    int API_ERROR = 2;

    int INVALID_COMMAND = 100;
    int INVALID_ARGS = 101;
    int MISSING_ARGS = 102;
    int MISSING_FILE_UPLOAD = 103;
    int INVALID_FILE_UPLOAD = 104;
    int INVALID_FILE_FORMAT = 105;

    int INSUFFICIENT_PERMISSIONS = 110;

    int INSUFFICIENT_BOT_PERMISSIONS = 121;

    int CONTENT_POLICY_VIOLATION = 200;

    int INVALID_CONFIG = 300;

    static void handleCommand(int status, CommandEvent event) {
        if (event.getSelfMember().hasPermission(Permission.MESSAGE_ADD_REACTION)
                && event.getSelfMember().hasPermission(Permission.MESSAGE_WRITE)) {

            switch (status) {
                case OKAY:
                    event.getMessage().addReaction("\u2705").queue();
                    break;
                case INTERNAL_SERVER_ERROR:
                    event.getMessage().addReaction("\u274C").queue();
                    event.reply(Messages.getErrorResponse("Internal Server Error", "INTERNAL_SERVER_ERROR",
                            "A server-side error occurred. That's all we know.",
                            INTERNAL_SERVER_ERROR));
                    break;
                case API_ERROR:
                    event.getMessage().addReaction("\u274C").queue();
                    event.reply(Messages.getErrorResponse("API Error", "API_ERROR",
                            "Error occurred attempting to utilize the API.",
                            API_ERROR));
                    break;
                case INVALID_COMMAND:
                    event.getMessage().addReaction("\u274C").queue();
                    event.reply(Messages.getErrorResponse("Invalid Command", "INVALID_COMMAND",
                            "Command not be processed as requested.",
                            INVALID_COMMAND));
                    break;
                case INVALID_ARGS:
                    event.getMessage().addReaction("\u274C").queue();
                    event.reply(Messages.getErrorResponse("Invalid Arguments", "INVALID_ARGS",
                            "Command was supplied arguments that were not valid for the command.",
                            INVALID_ARGS));
                    break;
                case MISSING_ARGS:
                    event.getMessage().addReaction("\u274C").queue();
                    event.reply(Messages.getErrorResponse("Missing Arguments", "MISSING_ARGS",
                            "Command required arguments that were not supplied.",
                            MISSING_ARGS));
                    break;
                case MISSING_FILE_UPLOAD:
                    event.getMessage().addReaction("\u274C").queue();
                    event.reply(Messages.getErrorResponse("Missing File Upload", "MISSING_FILE_UPLOAD",
                            "Command required file upload that was not supplied.",
                            MISSING_FILE_UPLOAD));
                    break;
                case INVALID_FILE_UPLOAD:
                    event.getMessage().addReaction("\u274C").queue();
                    event.reply(Messages.getErrorResponse("Invalid File Upload", "INVALID_FILE_UPLOAD",
                            "Uploaded file is not valid for this operation.",
                            INVALID_FILE_UPLOAD));
                    break;
                case INVALID_FILE_FORMAT:
                    event.getMessage().addReaction("\u274C").queue();
                    event.reply(Messages.getErrorResponse("Invalid File Format", "INVALID_FILE_UPLOAD",
                            "Uploaded file was of an invalid type or format and could not be used.",
                            INVALID_FILE_UPLOAD));
                    break;
                case INSUFFICIENT_PERMISSIONS:
                    event.getMessage().addReaction("\u274C").queue();
                    event.reply(Messages.getErrorResponse("Insufficient Permissions", "INSUFFICIENT_PERMISSIONS",
                            "User was not authorized to perform the operation.",
                            INSUFFICIENT_PERMISSIONS));
                    break;
                case INSUFFICIENT_BOT_PERMISSIONS:
                    event.getMessage().addReaction("\u274C").queue();
                    event.reply(Messages.getErrorResponse("Insufficient Bot Permissions", "INSUFFICIENT_BOT_PERMISSIONS",
                            "Bot did not have sufficient permissions to perform the operation.",
                            INSUFFICIENT_BOT_PERMISSIONS));
                    break;
                case INVALID_CONFIG:
                    event.getMessage().addReaction("\u274C").queue();
                    event.reply(Messages.getErrorResponse("Invalid Config", "INVALID_CONFIG",
                            "Operation could not be completed due to an improper server-side configuration.",
                            INVALID_CONFIG));
                    break;
                default:
                    event.getMessage().addReaction("\u274C").queue();
                    event.reply(Messages.getErrorResponse("Unknown ", "UNKNOWN",
                            "Operation returned with an unknown exit status.",
                            status));
                    break;
            }
        }
    }
}