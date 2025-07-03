package me.mmmjjkx.titlechanger.fabric.utils;

import net.minecraft.network.chat.ClickEvent;
import org.apache.commons.lang3.reflect.ConstructorUtils;

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
                Constructor<ClickEvent> constructor1 = (Constructor<ClickEvent>) ConstructorUtils.getAccessibleConstructor(Class.forName("net.minecraft.class_2558$class_10607"), File.class);
                Constructor<ClickEvent> constructor2 = (Constructor<ClickEvent>) ConstructorUtils.getAccessibleConstructor(Class.forName("net.minecraft.class_2558$class_10608"), URI.class);

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
}
