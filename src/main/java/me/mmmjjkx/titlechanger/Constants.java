package me.mmmjjkx.titlechanger;

import io.github.lijinhong11.titlechanger.api.TitleExtensionSource;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Constants {
    public static final String NO_RESULT = "NO_RESULT" + TitleExtensionSource.class.hashCode();

    public static final Logger LOGGER = Logger.getLogger("TitleChanger");

    public static final String RESOURCE_SETTINGS_FILE = "titlechanger/resource_settings";
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

    public static final List<String> WELCOME_SCREEN_TEXT_ERR = List.of(
            "Failed to load welcome text.",
            "See log file for details");

    public static final String HOUR_REPLACE = "%h";
    public static final String MINUTE_REPLACE = "%m";
    public static final String SECOND_REPLACE = "%s";
    public static final String HOUR_REPLACE_N2 = "%2h";
    public static final String MINUTE_REPLACE_N2 = "%2m";
    public static final String SECOND_REPLACE_N2 = "%2s";
}
