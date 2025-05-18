package me.mmmjjkx.titlechanger;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {
    public static final String SPLASH_TEXT_FILE = "splash.txt";
    public static final String RESOURCE_SETTINGS_FILE = "titlechanger/resource_settings";
    public static final String CONFIG_FILE = "titlechanger/config";
    public static final String ICON_FOLDER = "titlechanger/icons";

    public static final String WELCOME_SCREEN_TEXT_DEFAULT = """
            [TITLE] Welcome to %modpackName%
            Here's the welcome screen made by TitleChanger.
            Now TitleChanger isn't only a title changer.
            
            You can edit the screen text from "./configs/titlechanger/welcome_screen.txt".
            
            Multi language support?
            You can create another welcome text file like welcome_text_zh_CN.txt
            So when the user switch to Chinese (Simplified),
            it will show the text in welcome_text_zh_CN.txt
            Fallback to the default welcome text if the corresponding file isn't exists.
            
            It supports color text(It supports & and § for colorize):
            - &0Black
            - &1Dark Blue
            - &2Dark Green
            - &3Dark Aqua
            - &4Dark Red
            - &5Dark Purple
            - &6Gold
            - &7Gray
            - &8Dark Gray
            - &9Blue
            - &aGreen
            - &bAqua
            - &cRed
            - &dLight Purple
            - &eYellow
            - &fWhite (Actually colorized)
            - &lBold
            - &uUnderlined
            - &oItalic
            - &mStrikethrough
            - &kObfuscated
            - &rReset
            
            [h1] This is a header with level 1
            A text here
            
            [h2] This is a header with level 2
            
            [h3] This is a header with level 3
            
            Here's a clickable link
            https://modrinth.com/mod/titlechanger-next
            
            You can also click the link [here;https://modrinth.com/mod/titlechanger-next]
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
                String title = raw.get(0).replace("[TITLE] ", "");
                return Pair.of(title, raw.subList(1, raw.size()));
            } catch (IOException e) {
                return Pair.of("ERROR", WELCOME_SCREEN_TEXT_ERR);
            }
        } else {
            raw = Arrays.asList(WELCOME_SCREEN_TEXT_DEFAULT.split("\n"));
            try {
                file.createNewFile();
                Files.write(file.toPath(), raw);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Pair.of(raw.get(0).replace("[TITLE] ", ""), raw.subList(1, raw.size()));
    }
}
