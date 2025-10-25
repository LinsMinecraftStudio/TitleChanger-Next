package me.mmmjjkx.titlechanger.fabric.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.ClickEvent;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.joml.Matrix3x2fStack;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

public class Reflects {
    private static Constructor<?> openUrlConstructor = null;
    private static Constructor<?> openFileConstructor = null;
    private static Constructor<?> commonConstructor = null;

    static {
        Constructor<ClickEvent> constructor = ConstructorUtils.getAccessibleConstructor(ClickEvent.class, ClickEvent.Action.class, String.class);

        if (constructor == null) {
            Constructor<?> constructor1 = ConstructorUtils.getAccessibleConstructor(ClickEvent.OpenFile.class/*Class.forName("net.minecraft.class_2558$class_10607")*/, File.class);
            Constructor<?> constructor2 = ConstructorUtils.getAccessibleConstructor(ClickEvent.OpenUrl.class/*Class.forName("net.minecraft.class_2558$class_10608")*/, URI.class);

            openFileConstructor = constructor1;
            openUrlConstructor = constructor2;
        } else {
            commonConstructor = constructor;
        }
    }

    public static ClickEvent createOpenUrl(String url) {
        if (commonConstructor == null) {
            try {
                return (ClickEvent) openUrlConstructor.newInstance(URI.create(url));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return (ClickEvent) commonConstructor.newInstance(ClickEvent.Action.OPEN_URL, url);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static ClickEvent createOpenFile(File file) {
        if (commonConstructor == null) {
            try {
                return (ClickEvent) openFileConstructor.newInstance(file);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return (ClickEvent) commonConstructor.newInstance(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String getCurrentVersion() {
        return FabricLoader.getInstance()
                .getModContainer("minecraft")
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    public static void pushPose(Object pose) {
        if (pose instanceof Matrix3x2fStack m) {
            m.pushMatrix();
        } else {
            try {
                Method m = pose.getClass().getMethod("method_22903");
                m.invoke(pose);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void scale(Object pose, float x, float y, float z) {
        if (pose instanceof Matrix3x2fStack m) {
            m.scale(x, y);
        } else {
            try {
                Method m = pose.getClass().getMethod("method_22905", float.class, float.class, float.class);
                m.invoke(pose, x, y, z);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void translate(Object pose, float x, float y, float z) {
        if (pose instanceof Matrix3x2fStack m) {
            m.translate(x, y);
        } else {
            try {
                Method m = pose.getClass().getMethod("method_46416", float.class, float.class, float.class);
                m.invoke(pose, x, y, z);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void popPose(Object pose) {
        if (pose instanceof Matrix3x2fStack m) {
            m.popMatrix();
        } else {
            try {
                Method m = pose.getClass().getMethod("method_22909");
                m.invoke(pose);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
