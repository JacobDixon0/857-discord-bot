/*
 * Name: 857-discord-bot
 * Date: 2020/1/13
 * Author(s): jd@jacobdixon.us (Jacob Dixon)
 * Version: 1.0a
 */

package us.jacobdixon.discord;

public class Config<T> {
    private T value;
    private String key;
    private boolean isVolatile;
    private boolean isExternal;

    public Config() {
    }

    public Config(T value, String key, boolean isVolatile, boolean isExternal) {
        this.value = value;
        this.key = key;
        this.isVolatile = isVolatile;
        this.isExternal = isExternal;
    }

    public Config(T value, String key, boolean isVolatile) {
        this.value = value;
        this.key = key;
        this.isVolatile = isVolatile;
    }

    public Config(T value, String key) {
        this.value = value;
        this.key = key;
    }

    public Config(String key) {
        this.key = key;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public boolean isVolatile() {
        return isVolatile;
    }

    public void setVolatile(boolean aVolatile) {
        isVolatile = aVolatile;
    }

    public boolean isExternal() {
        return isExternal;
    }

    public void setExternal(boolean external) {
        isExternal = external;
    }
}
