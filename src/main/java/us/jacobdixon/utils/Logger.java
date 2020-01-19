/*
 * Name: 857-discord-bot
 * Date: 2020/1/19
 * Author(s): jd@jacobdixon.us (Jacob Dixon)
 * Version: 1.0a
 */

package us.jacobdixon.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public Logger(){
    }

    public enum LogPriority {
        INFO, WARNING, ERROR, FATAL_ERROR, DEBUG;
    }

    public void log(Exception e) {
        System.err.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()) + "EXCEPTION CAUGHT:");
        e.printStackTrace();

    }

    public void log(String message) {
        log(LogPriority.INFO, message);
    }

    /**
     * @param priority 0 = FATAL_ERROR, 1 = ERROR, 2 = WARNING, 3 = INFO, 4 = DEBUG
     */
    public void log(int priority, String message){
        if(priority == 0){
            log(LogPriority.FATAL_ERROR, message);
        } else if (priority == 1){
            log(LogPriority.ERROR, message);
        } else if (priority == 2){
            log(LogPriority.WARNING, message);
        } else if (priority == 3){
            log(LogPriority.INFO, message);
        } else if (priority == 4){
            log(LogPriority.DEBUG, message);
        }
    }

    public void log(LogPriority priority, String message) {
        if (priority == LogPriority.INFO) {
            System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()) + "INFO: " + message);
        } else if (priority == LogPriority.WARNING) {
            System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()) + "WARNING: " + message);
        } else if (priority == LogPriority.ERROR) {
            System.err.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()) + "ERROR: " + message);
        } else if (priority == LogPriority.FATAL_ERROR) {
            System.err.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()) + "FATAL ERROR: " + message);
        } else if (priority == LogPriority.DEBUG) {
            System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date()) + "DEBUG: " + message);
        }
    }

}
