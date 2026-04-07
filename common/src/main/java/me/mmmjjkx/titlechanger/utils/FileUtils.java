package me.mmmjjkx.titlechanger.utils;

import it.unimi.dsi.fastutil.Pair;
import me.mmmjjkx.titlechanger.Constants;
import org.apache.commons.lang3.Strings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class FileUtils {
    public static List<String> readSplashText(File cfgDir) {
        File splash = new File(cfgDir, "titlechanger/splash.txt");
        if (splash.exists()) {
            try {
                return Files.readAllLines(splash.toPath());
            } catch (IOException e) {
                return List.of("");
            }
        } else {
            try {
                splash.createNewFile();
            } catch (IOException e) {
                Constants.LOGGER.log(Level.SEVERE, "Failed to create splash.txt", e);
            }
            return List.of("");
        }
    }

    public static Pair<String, List<String>> readWelcomeText(File cfgDir, String lang) {
        File folder = new File(cfgDir, "titlechanger/welcome");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String name = lang.equalsIgnoreCase("en_US") ? "welcome_text.txt" : "welcome_text_" + lang + ".txt";
        File file = new File(folder, name);

        List<String> raw;

        if (file.exists()) {
            try {
                raw = Files.readAllLines(file.toPath());

                if (raw.isEmpty()) {
                    return Pair.of("???", List.of("The file is empty!"));
                }

                String title = "";

                if (Strings.CS.startsWith(raw.getFirst(), "[TITLE] ")) {
                    title = Strings.CS.replace(raw.getFirst(), "[TITLE] ", "", 1);
                    raw = raw.subList(1, raw.size());
                }

                return Pair.of(title, raw);
            } catch (IOException e) {
                Constants.LOGGER.log(Level.SEVERE, "Failed to read welcome text", e);
                return Pair.of("ERROR", Constants.WELCOME_SCREEN_TEXT_ERR);
            }
        } else {
            //go back to the default file
            try {
                File defaultFile = new File(cfgDir, "titlechanger/welcome/welcome_text.txt");
                if (!defaultFile.exists()) {
                    defaultFile.createNewFile();
                    Files.write(defaultFile.toPath(), Arrays.asList(Constants.WELCOME_SCREEN_TEXT_DEFAULT.split("\n")));
                }

                raw = Files.readAllLines(defaultFile.toPath());

                if (raw.isEmpty()) {
                    return Pair.of("???", List.of("The file is empty!"));
                }

                String title = "";

                if (Strings.CS.startsWith(raw.getFirst(), "[TITLE] ")) {
                    title = Strings.CS.replace(raw.getFirst(), "[TITLE] ", "", 1);
                    raw = raw.subList(1, raw.size());
                }

                return Pair.of(title, raw);
            } catch (IOException e) {
                Constants.LOGGER.log(Level.SEVERE, "Failed to read welcome text", e);
                return Pair.of("ERROR", Constants.WELCOME_SCREEN_TEXT_ERR);
            }
        }
    }
}
