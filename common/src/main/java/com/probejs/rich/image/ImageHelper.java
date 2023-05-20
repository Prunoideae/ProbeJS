package com.probejs.rich.image;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class ImageHelper {

    public static RenderTarget init() {
        RenderTarget frameBuffer = new TextureTarget(32, 32, true, false);
        frameBuffer.bindRead();
        GL11.glTexImage2D(3553, 0, 6408, frameBuffer.width, frameBuffer.height, 0, 6408, 5121, (java.nio.ByteBuffer) null);
        return frameBuffer;
    }

    public static NativeImage getFromItem(ItemStack stack, RenderTarget fbo) {
        fbo.bindWrite(true);
        renderItem(stack, Minecraft.getInstance().getItemRenderer());
        NativeImage image = fromRenderTarget(fbo);
        fbo.unbindWrite();
        return image;
    }

    public static NativeImage fromRenderTarget(RenderTarget frame) {
        NativeImage img = new NativeImage(frame.width, frame.height, true);
        frame.bindRead();
        img.downloadTexture(0, false);
        img.flipY();
        return img;
    }

    public static void renderItem(ItemStack itemStack, ItemRenderer itemRenderer) {
        Matrix4f backup = RenderSystem.getProjectionMatrix().copy();
        Matrix4f projection = Matrix4f.orthographic(0.0f, 16.0f, 0.0f, 16.0f, -150f, 150f);
        Matrix4f modelView = Matrix4f.createTranslateMatrix(1.0e-4f, 1.0e-4f, 0.0f);
        projection.multiply(modelView);
        RenderSystem.setProjectionMatrix(projection);
        itemRenderer.renderGuiItem(itemStack, 0, 0);
        RenderSystem.setProjectionMatrix(backup);
    }
}
