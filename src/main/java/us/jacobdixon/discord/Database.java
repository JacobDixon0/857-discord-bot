/*
 * Author: jd@jacobdixon.us (Jacob R. Dixon)
 * Date: 2020-03-31
 * Project: 857-discord-bot
 * Version: 1.1a
 * Repo: https://github.com/JacobDixon0/857-discord-bot
 */

package us.jacobdixon.discord;

import com.google.common.collect.HashBiMap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.parser.ParseException;
import us.jacobdixon.discord.configs.GlobalConfig;
import us.jacobdixon.discord.configs.GuildConfig;
import us.jacobdixon.discord.email.Email;
import us.jacobdixon.discord.email.EmailUser;
import us.jacobdixon.discord.exceptions.InvalidConfigException;
import us.jacobdixon.discord.files.SimpleFile;
import us.jacobdixon.utils.Logger;
import us.jacobdixon.utils.StringToolbox;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Database implements ReturnCodes {

    private static final String DB_GUILDS_PATH = "/guilds/";
    private static final String DB_CACHE_PATH = "/cache/";
    private static final String DB_STATIC_CACHE_PATH = "/cache/static/";
    private static final String DB_TEMP_CACHE_PATH = "/cache/tmp/";

    private static final String DB_GUILD_CACHE_PATH = "/cache/";
    private static final String DB_GUILD_TEMP_CACHE_PATH = "/cache/tmp/";
    private static final String DB_GUILD_EMAIL_CACHE_PATH = "/cache/email/";
    private static final String DB_GUILD_CONFIG_PATH = "/guild-conf.json";

    private GlobalConfig globalConfig;
    private Logger logger;
    private String dbRootPath;
    private String dbGuildEntriesPath;

    private HashBiMap<Guild, AdvancedGuild> guildsMap;

    public Database(GlobalConfig globalConfig, Logger logger) {
        this.globalConfig = globalConfig;
        this.logger = logger;
        this.dbRootPath = globalConfig.getDatabaseLocation();
        this.dbGuildEntriesPath = dbRootPath + DB_GUILDS_PATH;
    }

    private File getGuildDatabaseEntryDirectory(AdvancedGuild guild) {
        return new File(dbGuildEntriesPath + guild.getGuild().getId());
    }

    private File getGuildCacheDirectory(AdvancedGuild guild) {
        return new File(getGuildDatabaseEntryDirectory(guild).getAbsolutePath() + DB_GUILD_CACHE_PATH);
    }

    private File getGuildTempCacheDirectory(AdvancedGuild guild) {
        return new File(getGuildDatabaseEntryDirectory(guild).getAbsolutePath() + DB_GUILD_TEMP_CACHE_PATH);
    }

    private File getGuildEmailCacheDirectory(AdvancedGuild guild) {
        return new File(getGuildDatabaseEntryDirectory(guild).getAbsolutePath() + DB_GUILD_EMAIL_CACHE_PATH);
    }

    private File getGuildConfigFile(AdvancedGuild guild) {
        return new File(getGuildDatabaseEntryDirectory(guild).getAbsolutePath() + DB_GUILD_CONFIG_PATH);
    }

    public String getRandCacheLocation(String seed) {
        if (!new File(dbRootPath + DB_CACHE_PATH + getDate() + "/" + DigestUtils.shaHex(seed).substring(0, 5) + "/").exists()) {
            return DB_CACHE_PATH + getDate() + "/" + DigestUtils.shaHex(seed).substring(0, 5) + "/";
        } else {
            return getRandCacheLocation(seed + Math.random());
        }
    }

    public ArrayList<String> cacheEmailAttachmentsGuild(Email email) throws IOException {
        ArrayList<String> urls = new ArrayList<>();

        AdvancedGuild targetGuild = null;

        for (AdvancedGuild guild : getAdvancedGuilds()) {
            for (EmailUser destination : guild.getWhitelistedEmailDestinations()) {
                if (email.getDestinationUsers().get(0).getAddress().equals(destination.getAddress())) {
                    targetGuild = guild;
                    break;
                }
            }
        }

        if (targetGuild != null) {
            urls = cacheEmailAttachmentsGuild(email, targetGuild);
        } else {
            logger.log(Logger.LogPriority.WARNING, "No guild found with whitelisted destination \"" + email.getDestinationUsers().get(0).getAddress() + "\"");
        }

        return urls;
    }

    public ArrayList<String> cacheEmailAttachmentsGuild(Email email, AdvancedGuild guild) throws IOException {
        return cacheEmailAttachments(email, DB_GUILDS_PATH + guild.getGuild().getId() + DB_GUILD_EMAIL_CACHE_PATH + getDate());
    }

    public ArrayList<String> cacheEmailAttachments(Email email) throws IOException {
        String path = getRandCacheLocation(email.getMessage().getId());
        return cacheEmailAttachments(email, path);
    }

    public ArrayList<String> cacheEmailAttachments(Email email, String path) throws IOException {
        ArrayList<String> urls = new ArrayList<>();

        if (path.toCharArray()[path.length() - 1] != '/') {
            path += "/";
        }

        if (path.toCharArray()[0] != '/') {
            path = "/" + path;
        }

        for (SimpleFile file : email.getAttachments()) {
            File parentFile = new File(dbRootPath + path);
            buildDirectory(parentFile);
            file.writeTo(parentFile);

            urls.add("https://" + globalConfig.getDomain() + path + StringToolbox.sanitizeURL(file.getFilename()));
        }

        return urls;
    }

    public void cacheFile(String url) throws IOException {
        String name = "file";
        Matcher filenameMatcher = Pattern.compile("^https?://.+/(.+)$").matcher(url);
        if (filenameMatcher.find()) {
            name = filenameMatcher.group(1);
        }

        cacheFile(url, name);
    }

    public void cacheFile(String url, String name) throws IOException {

        String filePath = dbRootPath + getRandCacheLocation(url) + name;
        File file = new File(filePath);

        if (!file.exists()) {
            buildDirectory(file.getParentFile());
        }

        ReadableByteChannel byteChannel = Channels.newChannel(new URL(url).openStream());
        FileOutputStream fileOut = new FileOutputStream(filePath);

        fileOut.getChannel().transferFrom(byteChannel, 0, Long.MAX_VALUE);
        fileOut.close();
        byteChannel.close();

        if (!name.matches("^.+\\.[\\w-_$~]+$")) {

            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            String type = URLConnection.guessContentTypeFromStream(bufferedInputStream);
            bufferedInputStream.close();
            fileInputStream.close();

            boolean success = false;

            switch (type) {
                case "image/png":
                    success = file.renameTo(new File(file.getAbsolutePath() + ".png"));
                    break;
                case "image/jpeg":
                    success = file.renameTo(new File(file.getAbsolutePath() + ".jpg"));
                    break;
                case "image/gif":
                    success = file.renameTo(new File(file.getAbsolutePath() + ".gif"));
                    break;
                case "image/svg+xml":
                    success = file.renameTo(new File(file.getAbsolutePath() + ".svg"));
                    break;
                case "image/heif":
                case "image/heic":
                    success = file.renameTo(new File(file.getAbsolutePath() + ".heic"));
                    break;
                case "image/webp":
                    success = file.renameTo(new File(file.getAbsolutePath() + ".webp"));
                    break;
                case "image/x-icon":
                    success = file.renameTo(new File(file.getAbsolutePath() + ".ico"));
                    break;
                case "image/bmp":
                    success = file.renameTo(new File(file.getAbsolutePath() + ".bmp"));
                    break;

            }

            if (!success) {
                logger.log(Logger.LogPriority.WARNING, "Could not rename cached file to match type \"" + file.getAbsolutePath() + "\"");
            }
        }
    }

    public int userUploadGuildConfig(AdvancedGuild guild, File configFile) {
        int status;

        if (guild.getConfig() != null && guild.getConfig().checkValiditySimple(configFile)) {

            if (guild.getConfig().getLoadedConfigFile().delete() && configFile.renameTo(guild.getConfig().getLoadedConfigFile())) {
                status = OKAY;
            } else {
                logger.log(Logger.LogPriority.ERROR, "Could not replace guild config file \"" + guild.getConfig().getLoadedConfigFile().getAbsolutePath() + "\"");
                status = INTERNAL_SERVER_ERROR;
            }
        } else {
            status = INVALID_FILE_FORMAT;
            if (!configFile.delete()) {
                logger.log(Logger.LogPriority.ERROR, "Could not delete invalid guild config upload \"" + configFile.getAbsolutePath() + "\"");
            }
        }
        return status;
    }

    public int userUploadGlobalConfig(File configFile) {
        int status;

        if (globalConfig.checkValiditySimple(configFile)) {
            if (globalConfig.getLoadedConfigFile().delete() && configFile.renameTo(globalConfig.getLoadedConfigFile())) {
                status = OKAY;
            } else {
                status = INTERNAL_SERVER_ERROR;
            }
        } else {
            status = INVALID_FILE_FORMAT;
            if (!configFile.delete()) {
                logger.log(Logger.LogPriority.ERROR, "Could not delete invalid guild config upload \"" + configFile.getAbsolutePath() + "\"");
            }
        }

        return status;
    }

    public ArrayList<File> tempCacheMessageAttachmentsGuild(Message message) throws ExecutionException, InterruptedException {
        ArrayList<File> result = new ArrayList<>();

        for (Attachment attachment : message.getAttachments()) {
            result.add(attachment.downloadToFile(new File(getGuildDatabaseEntryDirectory(getAdvancedGuild(message.getGuild())) + DB_GUILD_TEMP_CACHE_PATH + "/" + attachment.getFileName())).get());
        }

        return result;
    }

    public ArrayList<File> tempCacheMessageAttachments(Message message) throws ExecutionException, InterruptedException {
        ArrayList<File> result = new ArrayList<>();

        for (Attachment attachment : message.getAttachments()) {
            result.add(attachment.downloadToFile(new File(dbRootPath + DB_TEMP_CACHE_PATH + attachment.getFileName())).get());
        }

        return result;
    }

    public void clearGuildTempCache() {
        for (AdvancedGuild guild : getAdvancedGuilds()) {
            clearGuildTempCache(guild);
        }
    }

    public void clearGuildTempCache(AdvancedGuild guild) {
        File guildTempCache = getGuildTempCacheDirectory(guild);

        if (guildTempCache.exists() && guildTempCache.isDirectory()) {
            File[] files = guildTempCache.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (!f.delete()) {
                        logger.log(Logger.LogPriority.ERROR, "Could not delete cached guild temp file \"" + f.getAbsolutePath() + "\"");
                    }
                }
            }
        }

    }

    public void clearGlobalTempCache() {
        File globalTempCache = new File(dbRootPath + DB_TEMP_CACHE_PATH);

        if (globalTempCache.exists() && globalTempCache.isDirectory()) {
            File[] files = globalTempCache.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (!f.delete()) {
                        logger.log(Logger.LogPriority.ERROR, "Could not delete cached temp file \"" + f.getAbsolutePath() + "\"");
                    }
                }
            }
        }

    }

    public void build(List<Guild> guilds) {
        logger.log("Building database...");

        File databaseRoot = new File(dbRootPath);
        File databaseGuildEntries = new File(dbGuildEntriesPath);
        File databaseGlobalCache = new File(dbRootPath + DB_CACHE_PATH);
        File databaseGlobalTempCache = new File(dbRootPath + DB_TEMP_CACHE_PATH);

        if (!databaseRoot.exists()) buildDirectory(databaseRoot);
        if (!databaseGuildEntries.exists()) buildDirectory(databaseGuildEntries);
        if (!databaseGlobalCache.exists()) buildDirectory(databaseGlobalCache);
        if (!databaseGlobalTempCache.exists()) buildDirectory(databaseGlobalTempCache);

        guildsMap = HashBiMap.create();

        for (Guild guild : guilds) {

            AdvancedGuild advancedGuild = new AdvancedGuild(guild);

            File guildDatabaseEntry = getGuildDatabaseEntryDirectory(advancedGuild);
            File guildDatabaseCache = getGuildCacheDirectory(advancedGuild);
            File guildDatabaseTempCache = getGuildTempCacheDirectory(advancedGuild);
            File guildDatabaseConfig = getGuildConfigFile(advancedGuild);

            if (!guildDatabaseEntry.exists()) buildDirectory(guildDatabaseEntry);
            if (!guildDatabaseCache.exists()) buildDirectory(guildDatabaseCache);
            if (!guildDatabaseTempCache.exists()) buildDirectory(guildDatabaseTempCache);
            if (!guildDatabaseConfig.exists()) {
                try {
                    buildDefaultGuildConfig(guildDatabaseConfig);
                } catch (FileNotFoundException e) {
                    logger.log(e, "Could not build new database entry for guild " + guild.getId() + " \"" + guild.getName() + "\"");
                    break;
                }
            }

            advancedGuild.getConfig().setLogger(logger);

            try {
                advancedGuild.getConfig().load(guildDatabaseConfig);
                guildsMap.put(guild, advancedGuild);
            } catch (IOException e) {
                logger.log(e, "Could not build new database entry for guild " + guild.getId() + " \"" + guild.getName() + "\"");
            } catch (ParseException | InvalidConfigException e) {
                logger.log(e, "Could not load config for guild " + guild.getId() + " \"" + guild.getName() + "\" due to an invalid config file");
            }
        }
    }

    private void buildDefaultGuildConfig(File path) throws FileNotFoundException {
        GuildConfig guildConfig = new GuildConfig().useDefaults();
        guildConfig.save(path);
    }

    private boolean buildDirectory(String path) {
        File f = new File(path);
        return buildDirectory(f);
    }

    private boolean buildDirectory(File path) {
        boolean scs = true;
        if (!path.exists()) {
            if (!path.getParentFile().exists()) {
                scs = path.getParentFile().mkdirs();
            }
            if (scs) scs = path.mkdir();
            if (!scs)
                logger.log(Logger.LogPriority.ERROR, "Could not make empty directory \"" + path.getAbsolutePath() + "\"");
        }
        return scs;
    }

    public ArrayList<Guild> getGuilds() {
        return new ArrayList<>(guildsMap.keySet());
    }

    public ArrayList<AdvancedGuild> getAdvancedGuilds() {
        return new ArrayList<>(guildsMap.inverse().keySet());
    }

    public Guild getGuild(AdvancedGuild advancedGuild) {
        return guildsMap.inverse().get(advancedGuild);
    }

    public AdvancedGuild getAdvancedGuild(Guild guild) {
        return guildsMap.get(guild);
    }

    public Guild getGuildById(String id) {
        return getAdvancedGuildById(id).getGuild();
    }

    public AdvancedGuild getAdvancedGuildById(String id) {
        for (AdvancedGuild guild : getAdvancedGuilds()) {
            if (id.equals(guild.getId())) {
                return guild;
            }
        }
        return null;
    }

    private static String getDate() {
        return new SimpleDateFormat("yyyy.MM.dd").format(new Date());
    }

    public void shutdown() {
        clearGuildTempCache();
        clearGlobalTempCache();
    }
}
