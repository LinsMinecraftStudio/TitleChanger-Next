package me.mmmjjkx.titlechanger.fabric.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;

public class Reflects {
    /*
    private static final VarHandle uuidHandle;

    private static final boolean serverTypeHandleEnabled;
     */

    static {
        /*
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
                uuidTemp = MethodHandles.privateLookupIn(User.class, MethodHandles.lookup()).findVarHandle(User.class, "field_1985", String.class);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }

        uuidHandle = uuidTemp;
        serverTypeHandleEnabled = b1;
         */
    }

    public static String getUserUUID(User usr) {
        return usr.getProfileId().toString();
    }

    public static boolean inRealms() {
        ClientPacketListener clientPacketListener = Minecraft.getInstance().getConnection();
        if (clientPacketListener != null && clientPacketListener.getConnection().isConnected()) {
            ServerData serverData = Minecraft.getInstance().getCurrentServer();
            return serverData != null && serverData.isRealm();
        }

        return false;
    }
}
