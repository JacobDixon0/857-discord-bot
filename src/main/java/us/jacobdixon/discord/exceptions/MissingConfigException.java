/*
 * Author: jd@jacobdixon.us (Jacob R. Dixon)
 * Date: 2020-03-31
 * Project: 857-discord-bot
 * Version: 1.1a
 * Repo: https://github.com/JacobDixon0/857-discord-bot
 */

package us.jacobdixon.discord.exceptions;

public class MissingConfigException extends ConfigException {
    public MissingConfigException(String message) {
        super(message);
    }
}
