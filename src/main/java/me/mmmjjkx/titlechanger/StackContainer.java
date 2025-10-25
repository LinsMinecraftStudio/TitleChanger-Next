package me.mmmjjkx.titlechanger;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix3x2fStack;

public record StackContainer(Object obj) {
    public static StackContainer of(PoseStack stack) {
        return new StackContainer((Object) stack);
    }

    public static StackContainer of(Matrix3x2fStack stack) {
        return new StackContainer((Object) stack);
    }

    public static StackContainer of(Object obj) {
        return new StackContainer(obj);
    }
}
