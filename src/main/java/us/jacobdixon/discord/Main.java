/*
 * Author: jd@jacobdixon.us (Jacob R. Dixon)
 * Date: 2020-03-31
 * Project: 857-discord-bot
 * Version: 1.1a
 * Repo: https://github.com/JacobDixon0/857-discord-bot
 */
package us.jacobdixon.discord;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.json.simple.parser.ParseException;
import us.jacobdixon.discord.configs.GlobalConfig;
import us.jacobdixon.discord.email.EmailHandler;
import us.jacobdixon.discord.exceptions.InvalidConfigException;
import us.jacobdixon.utils.Logger;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class Main {

    public static GlobalConfig globalConf = new GlobalConfig();
    public static Logger logger = new Logger(true);
    public static SimpleDateFormat timestampFormat;
    public static Database db;

    private static EventHandler defaultEventHandler;
    private static EmailHandler emailHandler;
    private static EventLogger eventLogger = new EventLogger(logger);

    private static CommandClientBuilder ccb = new CommandClientBuilder();

    protected static JDA jda;

    public static boolean running = false;
    private static int currentActivitiesIndex = 0;

    public static void main(String[] args) {

        try {
            globalConf.setLogger(logger);

            if (args.length > 0 && new File(args[0]).exists()) {
                globalConf.load(new File(args[0]));
            } else {
                globalConf.load(new File(globalConf.RUN_DIR + "/global-conf.json"));
            }

            timestampFormat = new SimpleDateFormat(globalConf.getTimestampFormat());
            logger.setTimestampFormat(timestampFormat.toPattern());
            db = new Database(globalConf, logger);
            defaultEventHandler = new EventHandler(db, logger);
            emailHandler = new EmailHandler(globalConf.getTokensPath(), globalConf.getCredentialsPath(), globalConf.getGmailUser(), db, logger);

        } catch (IOException | InvalidConfigException | ParseException e) {
            logger.log(e, "Exception caught loading global config", Logger.LogPriority.FATAL_ERROR);
            exit(-1);
        }

        ccb.setOwnerId(globalConf.getSuperAdmin());
        ccb.setPrefix(globalConf.getPrefix());
        ccb.setActivity(Activity.playing("Initializing..."));
        ccb.setStatus(OnlineStatus.IDLE);
        ccb.useHelpBuilder(false);
        ccb.addCommands(
                new Commands.PingCommand(),
                new Commands.ConfigCommand(),
                new Commands.EchoCommand(),
                new Commands.EchoFileCommand(),
                new Commands.AdminCommand(),
                new Commands.EchoEditCommand(),
                new Commands.PurgeCommand(),
                new Commands.ManualEventAnnounceCommand()
        );

        JDABuilder builder = JDABuilder.create(globalConf.getToken(), GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));
        builder.addEventListeners(eventLogger, defaultEventHandler, ccb.build());

        try {
            jda = builder.build().awaitReady();
            configureJDA(jda, globalConf);
        } catch (InterruptedException | LoginException e) {
            logger.log(e, "Exception caught building JDA", Logger.LogPriority.FATAL_ERROR);
            exit(-1);
        }

        db.build(jda.getGuilds());
        emailHandler.start();

        logger.log("Successfully started on host " + GlobalConfig.hostname + " running " + globalConf.OS_NAME);
        running = true;

        runtimeLoop();
    }

    private static void runtimeLoop() {
        while (running) {
            try {
                if (currentActivitiesIndex == globalConf.getActivityEntries().size() - 1) {
                    currentActivitiesIndex = 0;
                } else {
                    currentActivitiesIndex++;
                }

                configureActivity(globalConf.getActivityType(), currentActivitiesIndex);

                Thread.sleep(6000);
            } catch (InterruptedException | InvalidConfigException e) {
                logger.log(e);
            }

            if (!emailHandler.isRunning() && globalConf.getMode() != 2) {
                setBotMode(2);
            }
        }

        exit(0);
    }

    private static void configureJDA(JDA jda, GlobalConfig config) {
        logger.log("Configuring JDA...");

        if (config.getMode() == 0) {
            try {
                configureOnlineStatus(globalConf.getOnlineStatus());
            } catch (InvalidConfigException e) {
                logger.log(e, "Invalid option detected for \"" + globalConf.onlineStatus.getKey() + "\" while configuring JDA", Logger.LogPriority.WARNING);
            }
            try {
                configureActivity(config.getActivityType(), 0);
            } catch (InvalidConfigException e) {
                logger.log(e, "Invalid option detected for \"" + globalConf.onlineStatus.getKey() + "\" while configuring JDA", Logger.LogPriority.WARNING);
            }

        } else if (config.getMode() == 1) {
            jda.getPresence().setStatus(OnlineStatus.IDLE);
            jda.getPresence().setActivity(Activity.playing("\u26A0 Undergoing Maintenance"));
        } else if (config.getMode() == 2) {
            jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
            jda.getPresence().setActivity(Activity.playing("\u26A0 Limited Functionality"));
        } else if (config.getMode() == 3) {
            jda.getPresence().setStatus(OnlineStatus.INVISIBLE);
        } else {
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
            jda.getPresence().setActivity(Activity.playing("bot things"));
            logger.log(Logger.LogPriority.WARNING, "Invalid option detected for option \"" + config.mode.getKey() + "\" when configuring JDA, using default online status");
        }
    }

    public static void configureJDA() {
        configureJDA(jda, globalConf);
    }

    public static void configureActivity(long activityType, int activitiesIndex) throws InvalidConfigException {
        if (globalConf.getActivityEntries().get(0) != null && !globalConf.getActivityEntries().isEmpty()) {
            if (activityType == 0) {
                jda.getPresence().setActivity(Activity.playing(globalConf.getActivityEntries().get(activitiesIndex)));
            } else if (activityType == 1) {
                jda.getPresence().setActivity(Activity.listening(globalConf.getActivityEntries().get(activitiesIndex)));
            } else if (activityType == 2) {
                jda.getPresence().setActivity(Activity.watching(globalConf.getActivityEntries().get(activitiesIndex)));
            } else if (activityType == 3) {
                jda.getPresence().setActivity(Activity.streaming(globalConf.getActivityEntries().get(activitiesIndex), globalConf.getStreamingURL()));
            } else {
                jda.getPresence().setActivity(Activity.playing(globalConf.getActivityEntries().get(activitiesIndex)));
                throw new InvalidConfigException("Invalid activity type \"" + activityType + "\", using default value");
            }
        } else {
            jda.getPresence().setActivity(Activity.playing("bot things"));
            throw new InvalidConfigException("Activities list is empty, using default value");
        }
    }

    public static void configureOnlineStatus(long onlineStatus) throws InvalidConfigException {
        if (onlineStatus == 0) {
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
        } else if (onlineStatus == 1) {
            jda.getPresence().setStatus(OnlineStatus.IDLE);
        } else if (onlineStatus == 2) {
            jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
        } else if (onlineStatus == 3) {
            jda.getPresence().setStatus(OnlineStatus.INVISIBLE);
        } else {
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
            throw new InvalidConfigException("Invalid online status \"" + onlineStatus + "\", using default value");
        }
    }

    public static void setBotMode(long mode) {
        globalConf.setMode(mode);
        configureJDA();
    }

    public static void stop() {
        jda.shutdown();
        db.shutdown();
        running = false;
    }

    public static void exit(int status) {
        if (status != 0) {
            exit(status, "Exiting due to runtime error...");
        } else {
            exit(0, "Exiting...");
        }
    }

    public static void exit(int status, String logMsg) {
        if (status != 0) {
            logger.log(Logger.LogPriority.FATAL_ERROR, logMsg);
        } else {
            logger.log(Logger.LogPriority.INFO, logMsg);
        }
        System.exit(status);
    }

    public static EmailHandler getEmailHandler() {
        return emailHandler;
    }

}
