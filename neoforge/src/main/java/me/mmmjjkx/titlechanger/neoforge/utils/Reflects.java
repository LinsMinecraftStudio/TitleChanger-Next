package me.mmmjjkx.titlechanger.neoforge.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.network.chat.ClickEvent;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.joml.Matrix3x2fStack;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

@SuppressWarnings("unchecked")
public class Reflects {
    private static Constructor<ClickEvent> openUrlConstructor = null;
    private static Constructor<ClickEvent> openFileConstructor = null;
    private static Constructor<ClickEvent> commonConstructor = null;

    static {
        Constructor<ClickEvent> constructor = ConstructorUtils.getAccessibleConstructor(ClickEvent.class, ClickEvent.Action.class, String.class);

        if (constructor == null) {
            try {
                Constructor<ClickEvent> constructor1 = (Constructor<ClickEvent>) ConstructorUtils.getAccessibleConstructor(Class.forName("net.minecraft.network.chat.ClickEvent$OpenFile"), File.class);
                Constructor<ClickEvent> constructor2 = (Constructor<ClickEvent>) ConstructorUtils.getAccessibleConstructor(Class.forName("net.minecraft.network.chat.ClickEvent$OpenUrl"), URI.class);

                openFileConstructor = constructor1;
                openUrlConstructor = constructor2;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            commonConstructor = constructor;
        }
    }

    public static ClickEvent createOpenUrl(String url) {
        if (commonConstructor == null) {
            try {
                return openUrlConstructor.newInstance(URI.create(url));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return commonConstructor.newInstance(ClickEvent.Action.OPEN_URL, url);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static ClickEvent createOpenFile(File file) {
        if (commonConstructor == null) {
            try {
                return openFileConstructor.newInstance(file);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return commonConstructor.newInstance(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String getCurrentVersion() {
        WorldVersion version = SharedConstants.getCurrentVersion();
        Class<WorldVersion> clazz = WorldVersion.class;

        try {
            return (String) clazz.getMethod("getName").invoke(version);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return version.name();
        }
    }

    public static void pushPose(Object pose) {
        if (pose instanceof PoseStack ps) {
            ps.pushPose();
        } else if (pose instanceof Matrix3x2fStack m) {
            m.pushMatrix();
        }
    }

    public static void scale(Object pose, float x, float y, float z) {
        if (pose instanceof PoseStack ps) {
            ps.scale(x, y, z);
        } else if (pose instanceof Matrix3x2fStack m) {
            m.scale(x, y);
        }
    }

    public static void translate(Object pose, float x, float y, float z) {
        if (pose instanceof PoseStack ps) {
            ps.translate(x, y, z);
        } else if (pose instanceof Matrix3x2fStack m) {
            m.translate(x, y);
        }
    }

    public static void popPose(Object pose) {
        if (pose instanceof PoseStack ps) {
            ps.popPose();
        } else if (pose instanceof Matrix3x2fStack m) {
            m.popMatrix();
        }
    }
}
