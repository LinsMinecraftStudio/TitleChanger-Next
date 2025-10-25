package me.mmmjjkx.titlechanger.fabric.bulitin;

import io.github.lijinhong11.titlechanger.api.TitlePlaceholderExtension;
import me.mmmjjkx.titlechanger.Constants;
import me.mmmjjkx.titlechanger.fabric.TitleChangerFabric;
import me.mmmjjkx.titlechanger.fabric.utils.Reflects;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
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
    @Override
    public String getPlaceholderHeader() {
        return ""; // no header needed in core
    }

    @Override
    public String getStaticPlaceholderValue(String placeholder, String[] args) {
        if (!isIASInstalled()) {
            switch (placeholder) {
                case "playername" -> {
                    return Minecraft.getInstance().getUser().getName();
                }
                case "playeruuid" -> {
                    return Minecraft.getInstance().getUser().getProfileId().toString();
                }
            }
        }

        return switch (placeholder) {
            case "mcver" -> Reflects.getCurrentVersion();
            case "hitokoto" -> TitleChangerFabric.HITOKOTO;
            case "modpackName" -> TitleChangerFabric.getResourceSettings().modpackName;
            case "modpackVersion" -> TitleChangerFabric.getResourceSettings().modpackVersion;
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
            case "starttime" -> {
                if (args.length == 1) {
                    yield TitleChangerFabric.getStartTime(args[0]);
                } else {
                    yield TitleChangerFabric.getStartTime(TitleChangerFabric.getConfig().placeholderSettings.defaultTimeFormat);
                }
            }
            default -> Constants.NO_RESULT;
        };
    }

    @Override
    public String getDynamicPlaceholderValue(String placeholder, String[] args) {
        if (isIASInstalled()) {
            switch (placeholder) {
                case "playername" -> {
                    return Minecraft.getInstance().getUser().getName();
                }
                case "playeruuid" -> {
                    return Minecraft.getInstance().getUser().getProfileId().toString();
                }
            }
        }

        return switch (placeholder) {
            case "playingmode" -> getPlayingMode();
            case "fps" -> String.valueOf(Minecraft.getInstance().getFps());
            case "ping" -> getPing();
            case "playtime" -> getPlayTime();
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
            case "syncedtime" -> {
                if (args.length == 1) {
                    yield getSyncedTime(args[0]);
                } else {
                    yield getSyncedTime(TitleChangerFabric.getConfig().placeholderSettings.defaultTimeFormat);
                }
            }
            default -> Constants.NO_RESULT;
        };
    }

    private boolean isIASInstalled() {
        return FabricLoader.getInstance().isModLoaded("ias");
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
            } else if (clientPacketListener.getServerData() != null && clientPacketListener.getServerData().isRealm()) {
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
        format = StringUtils.replace(format, Constants.HOUR_REPLACE, String.valueOf(duration.toHoursPart()));
        format = StringUtils.replace(format, Constants.MINUTE_REPLACE, String.valueOf(duration.toMinutesPart()));
        format = StringUtils.replace(format, Constants.SECOND_REPLACE, String.valueOf(duration.toSecondsPart()));
        format = StringUtils.replace(format, Constants.HOUR_REPLACE_N2, String.format("%02d", duration.toHoursPart()));
        format = StringUtils.replace(format, Constants.MINUTE_REPLACE_N2, String.format("%02d", duration.toMinutesPart()));
        format = StringUtils.replace(format, Constants.SECOND_REPLACE_N2, String.format("%02d", duration.toSecondsPart()));
        return format;
    }

    @Override
    public List<String> getPlaceholders() {
        return List.of(
                "mcver",
                "hitokoto",
                "playingmode",
                "syncedtime",
                "playeruuid",
                "fps",
                "playername",
                "ping",
                "playtime",
                "starttime",
                "x", "y", "z",
                "luck",
                "modver",
                "modpackName",
                "modpackVersion"
        );
    }
}
