package com.probejs.rich;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.probejs.util.PlatformSpecial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
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

    public static NativeImage getFromFluid(Fluid fluid, RenderTarget fbo) {
        fbo.bindWrite(true);
        renderFluid(fluid);
        NativeImage image = fromRenderTarget(fbo);
        fbo.unbindWrite();
        return image;
    }

    public static NativeImage fromRenderTarget(RenderTarget frame) {
        NativeImage img = new NativeImage(frame.width, frame.height, true);
        frame.bindRead();
        img.downloadTexture(0, false);
        img.flipY();
        frame.unbindRead();
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

    public static void renderFluid(Fluid fluid) {
        TextureAtlasSprite sprite = PlatformSpecial.INSTANCE.get().getFluidSprite(fluid);
        Matrix4f backup = RenderSystem.getProjectionMatrix().copy();
        Matrix4f projection = Matrix4f.orthographic(0.0f, 16.0f, 0.0f, 16.0f, -150f, 150f);
        Matrix4f modelView = Matrix4f.createTranslateMatrix(1.0e-4f, 1.0e-4f, 0.0f);
        projection.multiply(modelView);
        RenderSystem.setProjectionMatrix(projection);
        renderFluidSprite(sprite);
        RenderSystem.setProjectionMatrix(backup);
    }

    public static void renderFluidSprite(TextureAtlasSprite sprite) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        PoseStack matrices = RenderSystem.getModelViewStack();
        Matrix4f matrix = matrices.last().pose();

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderTexture(0, sprite.atlas().location());
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        buffer.vertex(matrix, 0, 0, 0).color(255, 255, 255, 255).uv(u1, v0).endVertex();
        buffer.vertex(matrix, 0, 16, 0).color(255, 255, 255, 255).uv(u0, v0).endVertex();
        buffer.vertex(matrix, 16, 16, 0).color(255, 255, 255, 255).uv(u0, v1).endVertex();
        buffer.vertex(matrix, 16, 0, 0).color(255, 255, 255, 255).uv(u1, v1).endVertex();

        tesselator.end();
    }
}
