package me.mmmjjkx.titlechanger;

import io.github.lijinhong11.titlechanger.api.TitleExtensionSource;

import java.util.List;
import java.util.logging.Logger;

public class Constants {
    public static final String NO_RESULT = "NO_RESULT" + TitleExtensionSource.class.hashCode();

    public static final Logger LOGGER = Logger.getLogger("TitleChanger");

    public static final String CONFIG_FILE = "titlechanger/config";
    public static final String RESOURCE_SETTINGS_FILE = "titlechanger/resource_settings";
    public static final String ICON_FOLDER = "titlechanger/icons";

    public static final String WELCOME_SCREEN_TEXT_DEFAULT = """
            [TITLE] Welcome to %modpackName%

            This is a Markdown welcome screen. It supports *italic text*, **bold text**, ***bold italic text***,
            ~~strikethrough~~, `inline code`, and escaped characters such as \\*literal asterisks\\*.

            <gradient=#ff5f6d,#ffc371>This is an inline gradient</gradient> with Markdown around it: **bold** and *italic*.

            <center>This line is centered.</center>
            
            <right>This line is right-aligned.</right>

            ## Alignment and gradient

            <left>This line uses left alignment.</left>

            <center>This line uses center alignment.</center>
            
            <right>This line uses right alignment.</right>

            <center><gradient=#ff5f6d,#ffc371>Centered gradient text</gradient></center>
            
            <right>**Right-aligned bold text**</right>
            
            <left><gradient=#36d1dc,#5b86e5>Left-aligned blue gradient</gradient></left>

            > This is a block quote written with Markdown.

            ## Lists

            - First unordered item
            - Second unordered item with **bold text**
              - Nested items are also accepted by CommonMark

            1. First ordered item
            2. Second ordered item

            ---

            ## Links

            - [Open the TitleChanger project](https://modrinth.com/mod/titlechanger-next)
            - [Open the welcome folder](file://config/titlechanger/welcome)

            ## Tables

            | Name | Value |
            | ---- | ----- |
            | Mod | TitleChanger |
            | Version | %modver:titlechanger% |

            ## Images

            Use a local file or a web URL with standard Markdown image syntax:

            ![Local image](config/titlechanger/images/example.png)
            ![Example image](https://dummyimage.com/640x360/202020/ffffff.png&text=TitleChanger)
            ![Remote image](https://example.com/example.png)

            ## Code Blocks
            ```java
            // This is a normal Markdown code block.
            class Example {
              public static void main(String[] args) {
                System.out.println("It is rendered as gray code text.");
                System.out.println("Kinda amazing.");
              }
            }
            ```

            ## Minecraft colors

            The following are the 16 standard Minecraft chat colors:

            - &0black
            - &1dark blue
            - &2dark green
            - &3dark aqua
            - &4dark red
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
            - &fwhite
            
            ## Multi-language support

            Create a localized file such as `welcome_text_zh_cn.txt` in the welcome folder.
            The default welcome text is used when a localized file does not exist.

            **Note:** Clicking the `Done` button disables this welcome screen.
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
