/*
 * Author: jd@jacobdixon.us (Jacob R. Dixon)
 * Date: 2020-03-31
 * Project: 857-discord-bot
 * Version: 1.1a
 * Repo: https://github.com/JacobDixon0/857-discord-bot
 */

package us.jacobdixon.utils;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileToolbox {

    public static String getSHA1(File file){

        String hash = null;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            InputStream filein = new FileInputStream(file);
            int n = 0;
            byte[] buffer = new byte[8192];
            while(n != -1){
                n = filein.read(buffer);
                if (n > 0){
                    digest.update(buffer, 0, n);
                }
            }

            hash = new HexBinaryAdapter().marshal(digest.digest());

        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }

        return hash;
    }
}
