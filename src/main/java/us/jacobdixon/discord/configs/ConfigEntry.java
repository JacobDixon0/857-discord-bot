/*
 * Author: jd@jacobdixon.us (Jacob R. Dixon)
 * Date: 2020-03-31
 * Project: 857-discord-bot
 * Version: 1.1a
 * Repo: https://github.com/JacobDixon0/857-discord-bot
 */

package us.jacobdixon.discord.configs;

public class ConfigEntry<T> {

    private String key;
    private T value;
    private T defaultValue;
    private boolean requiresHandling = false;

    public ConfigEntry(String key) {
        this.key = key;
    }

    public ConfigEntry(String key, boolean requiresHandling) {
        this.key = key;
        this.requiresHandling = requiresHandling;
    }

    public ConfigEntry(String key, T defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public ConfigEntry(String key, T defaultValue, boolean requiresHandling) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.requiresHandling = requiresHandling;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean requiresHandling() {
        return requiresHandling;
    }

    public void setRequiresHandling(boolean requiresHandling) {
        this.requiresHandling = requiresHandling;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

}
