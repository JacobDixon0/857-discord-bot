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
    public static Logger logger = new Logger();
    public static SimpleDateFormat timestampFormat;
    public static Database db;

    private static EventHandler defaultEventHandler;
    private static EmailHandler emailHandler;
    private static EventLogger eventLogger = new EventLogger(logger);

    private static CommandClientBuilder ccb = new CommandClientBuilder();

    static JDA jda;

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

        } catch (IOException | ParseException | InvalidConfigException e) {
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
                new Commands.TestCommand(),
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

                if (globalConf.getActivityType() == 0) {
                    jda.getPresence().setActivity(Activity.playing(globalConf.getActivityEntries().get(currentActivitiesIndex)));
                } else if (globalConf.getActivityType() == 1) {
                    jda.getPresence().setActivity(Activity.listening(globalConf.getActivityEntries().get(currentActivitiesIndex)));
                } else if (globalConf.getActivityType() == 2) {
                    jda.getPresence().setActivity(Activity.watching(globalConf.getActivityEntries().get(currentActivitiesIndex)));
                } else if (globalConf.getActivityType() == 3) {
                    jda.getPresence().setActivity(Activity.streaming(globalConf.getActivityEntries().get(currentActivitiesIndex), globalConf.getStreamingURL()));
                } else {
                    jda.getPresence().setActivity(Activity.playing(globalConf.getActivityEntries().get(currentActivitiesIndex)));
                    logger.log(Logger.LogPriority.WARNING, "Invalid option detected for \"" + globalConf.activityType.getKey() + "\" when configuring JDA, defaulting to \"Playing\"");
                }

                Thread.sleep(6000);
            } catch (InterruptedException e) {
                logger.log(e);
            }
        }

        exit(0);
    }

    private static void configureJDA(JDA jda, GlobalConfig config) {
        logger.log("Configuring JDA...");

        if (config.getMode() == 0) {

            if (config.getActivityEntries().get(0) != null) {
                if (config.getActivityType() == 0) {
                    jda.getPresence().setActivity(Activity.playing(config.getActivityEntries().get(0)));
                } else if (config.getActivityType() == 1) {
                    jda.getPresence().setActivity(Activity.listening(config.getActivityEntries().get(0)));
                } else if (config.getActivityType() == 2) {
                    jda.getPresence().setActivity(Activity.watching(config.getActivityEntries().get(0)));
                } else if (config.getActivityType() == 3) {
                    jda.getPresence().setActivity(Activity.streaming(config.getActivityEntries().get(0), globalConf.getStreamingURL()));
                } else {
                    jda.getPresence().setActivity(Activity.playing(config.getActivityEntries().get(0)));
                    logger.log(Logger.LogPriority.WARNING, "Invalid option detected for \"" + config.activityType.getKey() + "\" when configuring JDA, defaulting to \"Playing\"");
                }
            } else {
                jda.getPresence().setActivity(Activity.playing("bot things"));
                logger.log(Logger.LogPriority.WARNING, "Invalid option detected for \"" + config.activityEntries.getKey() + "\" when configuring JDA, value cannot be null, defaulting to \"bot things\"");
            }

            if (config.getOnlineStatus() == 0) {
                jda.getPresence().setStatus(OnlineStatus.ONLINE);
            } else if (config.getOnlineStatus() == 1) {
                jda.getPresence().setStatus(OnlineStatus.IDLE);
            } else if (config.getOnlineStatus() == 2) {
                jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
            } else if (config.getOnlineStatus() == 3) {
                jda.getPresence().setStatus(OnlineStatus.INVISIBLE);
            } else {
                jda.getPresence().setStatus(OnlineStatus.ONLINE);
                logger.log(Logger.LogPriority.WARNING, "Invalid option detected for option \"" + config.onlineStatus.getKey() + "\" when configuring JDA, defaulting to \"ONLINE\"");
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

    public static EmailHandler getEmailHandler() {
        return emailHandler;
    }

    public static void stop() {
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
        if (status != -1) {
            jda.shutdown();
            db.shutdown();
        }
        System.exit(status);
    }

}
