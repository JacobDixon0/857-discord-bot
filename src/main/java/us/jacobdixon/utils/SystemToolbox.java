package us.jacobdixon.utils;

import java.io.IOException;
import java.util.Scanner;

public class SystemToolbox {
    public static String execReadToString(String execCommand) throws IOException {
        try (Scanner s = new Scanner(Runtime.getRuntime().exec(execCommand).getInputStream()).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }
}
