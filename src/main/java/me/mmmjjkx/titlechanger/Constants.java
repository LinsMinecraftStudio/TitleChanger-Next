package me.mmmjjkx.titlechanger;

import it.unimi.dsi.fastutil.Pair;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Constants {
    public static final String SPLASH_TEXT_FILE = "splash.txt";
    public static final String RESOURCE_SETTINGS_FILE = "titlechanger/resource_settings";
    public static final String CONFIG_FILE = "titlechanger/config";
    public static final String ICON_FOLDER = "titlechanger/icons";

    public static final Pattern LINK_PATTERN = Pattern.compile("<.+?;(file|http|https)://\\S+>");

    public static final String WELCOME_SCREEN_TEXT_DEFAULT = """
            [TITLE] Welcome to %modpackName%
            Here's the welcome screen made by TitleChanger.
            Now TitleChanger isn't only a title changer.
            
            Note: the welcome screen will automatically disabled if you click the 'Done' button.
            
            You can edit the screen text from "./config/titlechanger/welcome/welcome_screen.txt".
            You can open the folder that stores welcome text <here;file://config/titlechanger/welcome>
            
            Multi language support?
            You can create another welcome text file like welcome_text_zh_cn.txt
            So when the user switch to Chinese (Simplified),
            it will show the text in welcome_text_zh_cn.txt
            Fallback to the default welcome text if the corresponding file isn't exists.
            
            It supports color codes:
            - &0black
            - &1dark blue
            - &2dark green
            - &3dark aqua
            - &4rark red
            - &5dark purple
            - &6gold
            - &7gray
            - &8dark gray
            - &9blue
            - &agreen
            - &baqua
            - &cred
            - &dlight purple
            - &eyellow
            - &fwhite (Actually colorized)
            - &lbold
            - &nunderlined
            - &oitalic
            - &mstrikethrough
            - &kobfuscated &r(obfuscated)
            - &rreset
            
            You can combine the color codes:
            &a&mGreen and Strikethrough
            &b&nAqua and underline
            
            [h1] This is a header with level 1
            A text here
            
            [h2] This is a header with level 2
            Another text
            
            [h3] This is a header with level 3
            Text here too
            
            Here's a clickable link
            https://modrinth.com/mod/titlechanger-next
            
            You can also click the link <here;https://modrinth.com/mod/titlechanger-next>
            """;

    private static final List<String> WELCOME_SCREEN_TEXT_ERR = List.of(
            "Failed to load welcome text.",
            "That means the file with the name exists.",
            "But the file isn't readable!");

    public static Pair<String, List<String>> readWelcomeText(File cfgDir, String lang) {
        File cfg = new File(cfgDir, "titlechanger/welcome");
        if (!cfg.exists()) {
            cfg.mkdirs();
        }

        String name = lang.equalsIgnoreCase("en_US") ? "welcome_text.txt" : "welcome_text_" + lang + ".txt";
        File file = new File(cfg, name);

        List<String> raw;

        if (file.exists()) {
            try {
                raw = Files.readAllLines(file.toPath());

                if (raw.isEmpty()) {
                    return Pair.of("???", List.of("The file is empty!"));
                }

                String title = "";

                if (StringUtils.startsWith(raw.get(0), "[TITLE] ")) {
                    title = StringUtils.replace(raw.get(0), "[TITLE] ", "", 1);
                    raw = raw.subList(1, raw.size());
                }

                return Pair.of(title, raw);
            } catch (IOException e) {
                return Pair.of("ERROR", WELCOME_SCREEN_TEXT_ERR);
            }
        } else {
            //go back to the default file
            try {
                File defaultFile = new File(cfgDir, "titlechanger/welcome/welcome_text.txt");
                if (!defaultFile.exists()) {
                    defaultFile.createNewFile();
                    Files.write(defaultFile.toPath(), Arrays.asList(WELCOME_SCREEN_TEXT_DEFAULT.split("\n")));
                }

                raw = Files.readAllLines(defaultFile.toPath());

                if (raw.isEmpty()) {
                    return Pair.of("???", List.of("The file is empty!"));
                }

                String title = "";

                if (StringUtils.startsWith(raw.get(0), "[TITLE] ")) {
                    title = StringUtils.replace(raw.get(0), "[TITLE] ", "", 1);
                    raw = raw.subList(1, raw.size());
                }

                return Pair.of(title, raw);
            } catch (IOException e) {
                return Pair.of("ERROR", WELCOME_SCREEN_TEXT_ERR);
            }
        }
    }
}
