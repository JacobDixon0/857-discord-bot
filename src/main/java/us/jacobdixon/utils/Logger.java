/*
 * Author: jd@jacobdixon.us (Jacob R. Dixon)
 * Date: 2020-03-31
 * Project: 857-discord-bot
 * Version: 1.1a
 * Repo: https://github.com/JacobDixon0/857-discord-bot
 */

/*
 * Name: 857-discord-bot
 * Date: 2020/1/19
 * Author(s): jd@jacobdixon.us (Jacob Dixon)
 * Version: 1.0a
 */

package us.jacobdixon.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private SimpleDateFormat df = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ");

    private boolean usePrettyPrinting = false;

    public Logger() {
    }

    public Logger(boolean usePrettyPrinting) {
        this.usePrettyPrinting = usePrettyPrinting;
    }

    public enum LogPriority {
        INFO, WARNING, ERROR, FATAL_ERROR, DEBUG
    }

    public void log(Exception e) {
        logOut(">>> BEGIN EXCEPTION STACK TRACE >>>", true, Ansi.UNDERLINED);
        logOut(ExceptionUtils.getStackTrace(e).trim(), true);
        logOut("<<< END EXCEPTION STACK TRACE <<<", true, Ansi.UNDERLINED);
    }

    public void log(Exception e, String errDesc) {
        log(LogPriority.ERROR, errDesc);
        logOut(">>> BEGIN EXCEPTION STACK TRACE >>>", true, Ansi.UNDERLINED);
        logOut(ExceptionUtils.getStackTrace(e).trim(), true);
        logOut("<<< END EXCEPTION STACK TRACE <<<", true, Ansi.UNDERLINED);
    }

    public void log(Exception e, String errDesc, LogPriority priority) {
        log(priority, errDesc);
        logOut(">>> BEGIN EXCEPTION STACK TRACE >>>", true, Ansi.UNDERLINED);
        logOut(ExceptionUtils.getStackTrace(e).trim(), true);
        logOut("<<< END EXCEPTION STACK TRACE <<<", true, Ansi.UNDERLINED);
    }

    public void log(Object o) {
        System.out.println(o.toString());
    }

    public void log(String message) {
        log(LogPriority.INFO, message);
    }

    /**
     * @param priority 0 = FATAL_ERROR, 1 = ERROR, 2 = WARNING, 3 = INFO, 4 = DEBUG
     */
    public void log(int priority, String message) {
        if (priority == 0) {
            log(LogPriority.FATAL_ERROR, message);
        } else if (priority == 1) {
            log(LogPriority.ERROR, message);
        } else if (priority == 2) {
            log(LogPriority.WARNING, message);
        } else if (priority == 3) {
            log(LogPriority.INFO, message);
        } else if (priority == 4) {
            log(LogPriority.DEBUG, message);
        }
    }

    public void log(LogPriority priority, String message) {
        if (priority == LogPriority.INFO) {
            logOut("INFO: " + message);
        } else if (priority == LogPriority.WARNING) {
            logOut("WARN: " + message, true);
        } else if (priority == LogPriority.ERROR) {
            logOut("ERROR: " + message, true, Ansi.FG_RED);
        } else if (priority == LogPriority.FATAL_ERROR) {
            logOut("FATAL ERROR: " + message, true, Ansi.BG_RED, Ansi.FG_BRIGHT_WHITE);
        } else if (priority == LogPriority.DEBUG) {
            logOut("DEBUG: " + message, false, Ansi.BG_PURPLE, Ansi.FG_BRIGHT_WHITE);
        }
    }

    private void logOut(String string) {
        logOut(string, false);
    }

    private void logOut(String string, boolean errOut) {
        if (usePrettyPrinting) {
            if (errOut) {
                System.err.println(formatText(df.format(new Date()) + string, Ansi.FG_RED));
            } else {
                System.out.println(df.format(new Date()) + string);
            }
        } else {
            if (errOut) {
                System.err.println(df.format(new Date()) + string);
            } else {
                System.out.println(df.format(new Date()) + string);
            }
        }
    }

    private void logOut(String string, boolean errOut, String... attribs) {
        if (usePrettyPrinting) {
            if (errOut) {
                System.err.println(formatText(df.format(new Date()) + string, attribs));
            } else {
                System.out.println(formatText(df.format(new Date()) + string, attribs));
            }
        } else {
            logOut(string, errOut);
        }
    }

    private String formatText(String string, String... attribs) {
        StringBuilder formattedString = new StringBuilder();
        for(String s : attribs){
            formattedString.append(s);
        }
        formattedString.append(string);
        if (!formattedString.toString().endsWith(Ansi.RESET)) {
            formattedString.append(Ansi.RESET);
        }
        return formattedString.toString();
    }

    public void setTimestampFormat(String pattern) {
        this.df = new SimpleDateFormat("[" + pattern + "] ");
    }
}
