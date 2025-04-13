package me.mmmjjkx.titlechanger.fabric.bulitin;

import io.github.lijinhong11.titlechanger.api.TitlePlaceholderExtension;
import me.mmmjjkx.titlechanger.fabric.TitleChangerFabric;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.language.I18n;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SuppressWarnings("deprecation")
public class TCPlaceholders implements TitlePlaceholderExtension {
    @Override
    public String getPlaceholderHeader() {
        return ""; // no header needed in core
    }

    @Override
    public String getPlaceholderValue(String placeholder, String[] args) {
        return switch (placeholder) {
            case "mcver" -> SharedConstants.VERSION_STRING;
            case "hitokoto" -> TitleChangerFabric.HITOKOTO;
            case "playingmode" -> getPlayingMode();
            case "playername" -> Minecraft.getInstance().getUser().getName();
            case "playeruuid" -> Minecraft.getInstance().getUser().getUuid();
            case "starttime" -> {
                if (args.length == 1) {
                    yield TitleChangerFabric.getStartTime(args[0]);
                } else {
                    yield TitleChangerFabric.getStartTime(TitleChangerFabric.getConfig().placeholderSettings.defaultTimeFormat);
                }
            }
            case "syncedtime" -> {
                if (args.length == 1) {
                    yield getSyncedTime(args[0]);
                } else {
                    yield getSyncedTime(TitleChangerFabric.getConfig().placeholderSettings.defaultTimeFormat);
                }
            }
            default -> "%ERROR%";
        };
    }

    private String getPlayingMode() {
        Minecraft client = Minecraft.getInstance();
        ClientPacketListener clientPacketListener = client.getConnection();

        if (clientPacketListener != null && clientPacketListener.getConnection().isConnected()) {
            if (client.getSingleplayerServer() != null && !client.getSingleplayerServer().isPublished()) {
                return I18n.get("title.singleplayer");
            } else if (client.isConnectedToRealms()) {
                return I18n.get("title.multiplayer.realms");
            } else if (client.getSingleplayerServer() == null && (client.getCurrentServer() == null || !client.getCurrentServer().isLan())) {
                return I18n.get("title.multiplayer.other");
            } else {
                return I18n.get("title.multiplayer.lan");
            }
        }

        return "";
    }

    private String getSyncedTime(String format) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return now.format(formatter);
    }

    @Override
    public String getExtensionName() {
        return "titlechanger";
    }

    @Override
    public List<String> getPlaceholders() {
        return List.of("mcver", "hitokoto", "playingmode", "syncedtime", "playeruuid");
    }
}
