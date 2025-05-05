package me.mmmjjkx.titlechanger.neoforge.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ServerData;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class Reflects {
    private static final VarHandle uuidHandle;

    private static final boolean serverTypeHandleEnabled;

    static {
        //i hate mojang do that
        VarHandle uuidTemp = null;
        boolean b1 = false;
        try {
            for (Field field : User.class.getDeclaredFields()) {
                if (field.getType() == UUID.class) {
                    field.setAccessible(true);
                    uuidTemp = MethodHandles.privateLookupIn(User.class, MethodHandles.lookup()).findVarHandle(User.class, field.getName(), UUID.class);
                    b1 = true;
                    break;
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            try {
                uuidTemp = MethodHandles.privateLookupIn(User.class, MethodHandles.lookup()).findVarHandle(User.class, "uuid", String.class);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }

        uuidHandle = uuidTemp;
        serverTypeHandleEnabled = b1;
    }

    public static String getUserUUID(User usr) {
        return uuidHandle.get(usr).toString();
    }

    public static boolean inRealms() {
        Minecraft client = Minecraft.getInstance();
        if (client.getCurrentServer() != null) {
            if (serverTypeHandleEnabled) {
                try {
                    Field type = ServerData.class.getDeclaredField("type");
                    type.setAccessible(true);
                    Enum<?> type1 = (Enum<?>) type.get(client.getCurrentServer());
                    return type1.ordinal() == 2;
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    return false;
                }
            } else {
                try {
                    return (boolean) Minecraft.class.getMethod("isConnectedToRealms").invoke(client);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return false;
    }
}
