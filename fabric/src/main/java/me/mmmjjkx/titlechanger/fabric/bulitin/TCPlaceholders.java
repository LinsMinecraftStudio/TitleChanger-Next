package me.mmmjjkx.titlechanger.fabric.bulitin;

import io.github.lijinhong11.titlechanger.api.TitlePlaceholderExtension;
import me.mmmjjkx.titlechanger.fabric.TitleChangerFabric;
import me.mmmjjkx.titlechanger.fabric.utils.Reflects;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.language.I18n;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TCPlaceholders implements TitlePlaceholderExtension {
    private static final String HOUR_REPLACE = "%h";
    private static final String MINUTE_REPLACE = "%m";
    private static final String SECOND_REPLACE = "%s";
    private static final String HOUR_REPLACE_N2 = "%2h";
    private static final String MINUTE_REPLACE_N2 = "%2m";
    private static final String SECOND_REPLACE_N2 = "%2s";

    @Override
    public String getPlaceholderHeader() {
        return ""; // no header needed in core
    }

    @Override
    public String getPlaceholderValue(String placeholder, String[] args) {
        return switch (placeholder) {
            case "mcver" -> SharedConstants.getCurrentVersion().getName();
            case "hitokoto" -> TitleChangerFabric.HITOKOTO;
            case "playingmode" -> getPlayingMode();
            case "playername" -> Minecraft.getInstance().getUser().getName();
            case "playeruuid" -> Reflects.getUserUUID(Minecraft.getInstance().getUser());
            case "fps" -> String.valueOf(Minecraft.getInstance().getFps());
            case "ping" -> getPing();
            case "playtime" -> getPlayTime();
            case "modver" -> {
                if (args.length == 1) {
                    String modid = args[0];
                            Optional<ModContainer> optional = FabricLoader.getInstance().getModContainer(modid);
                    if (optional.isPresent()) {
                        yield optional.get().getMetadata().getVersion().getFriendlyString();
                    }

                    yield "%ERROR: no mod found%";
                }

                yield "%ERROR: no modid specified%";
            }
            case "luck" -> {
                if (Minecraft.getInstance().player != null) {
                    yield String.valueOf(Minecraft.getInstance().player.getLuck());
                }

                yield "?";
            }
            case "x" -> {
                if (Minecraft.getInstance().player != null) {
                    yield String.valueOf(Minecraft.getInstance().player.getX());
                }

                yield "?";
            }
            case "y" -> {
                if (Minecraft.getInstance().player != null) {
                    yield String.valueOf(Minecraft.getInstance().player.getY());
                }

                yield "?";
            }
            case "z" -> {
                if (Minecraft.getInstance().player != null) {
                    yield String.valueOf(Minecraft.getInstance().player.getZ());
                }

                yield "?";
            }
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

    private String getPing() {
        Minecraft client = Minecraft.getInstance();
        if (client.getConnection() != null) {
            ClientPacketListener connection = client.getConnection();
            PlayerInfo info = connection.getPlayerInfo(client.getUser().getName());
            if (info != null) {
                return String.valueOf(info.getLatency());
            }
        }

        return "0";
    }

    private String getPlayingMode() {
        Minecraft client = Minecraft.getInstance();
        ClientPacketListener clientPacketListener = client.getConnection();
        if (clientPacketListener != null && clientPacketListener.getConnection().isConnected()) {
            if (client.getSingleplayerServer() != null && !client.getSingleplayerServer().isPublished()) {
                return I18n.get("title.singleplayer");
            } else if (Reflects.inRealms()) {
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

    private String getPlayTime() {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(TitleChangerFabric.getStartTime(), now);
        String format = TitleChangerFabric.getConfig().placeholderSettings.playTimeFormat;
        format = StringUtils.replace(format, HOUR_REPLACE, String.valueOf(duration.toHoursPart()));
        format = StringUtils.replace(format, MINUTE_REPLACE, String.valueOf(duration.toMinutesPart()));
        format = StringUtils.replace(format, SECOND_REPLACE, String.valueOf(duration.toSecondsPart()));
        format = StringUtils.replace(format, HOUR_REPLACE_N2, String.format("%02d", duration.toHoursPart()));
        format = StringUtils.replace(format, MINUTE_REPLACE_N2, String.format("%02d", duration.toMinutesPart()));
        format = StringUtils.replace(format, SECOND_REPLACE_N2, String.format("%02d", duration.toSecondsPart()));
        return format;
    }

    @Override
    public String getExtensionName() {
        return "titlechanger";
    }

    @Override
    public List<String> getPlaceholders() {
        return List.of("mcver", "hitokoto", "playingmode", "syncedtime", "playeruuid", "fps", "playername", "ping", "playtime", "starttime", "x", "y", "z", "luck", "modver");
    }
}
