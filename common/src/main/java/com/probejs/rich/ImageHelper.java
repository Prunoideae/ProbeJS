package com.probejs.rich;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.probejs.ProbeJS;
import com.probejs.util.PlatformSpecial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class ImageHelper {

    private static Matrix4f createTranslateMatrix(float x, float y, float z) {
        return new Matrix4f()
                .m00(1.0f)
                .m11(1.0f)
                .m22(1.0f)
                .m33(1.0f)
                .m03(x)
                .m13(y)
                .m23(z);
    }

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
        Matrix4f backup;
        VertexSorting sorting = RenderSystem.getVertexSorting();
        try {
            backup = (Matrix4f) (RenderSystem.getProjectionMatrix().clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        Matrix4f projection = new Matrix4f().setOrtho(0.0f, 16.0f, 0.0f, 16.0f, -150f, 150f);
        Matrix4f modelView = createTranslateMatrix(1.0e-4f, 1.0e-4f, 0.0f);
        projection.mul(modelView);
        RenderSystem.setProjectionMatrix(projection, sorting);
        renderGuiItem(Minecraft.getInstance(), itemRenderer, itemStack, 0, 0);
        RenderSystem.setProjectionMatrix(backup, sorting);
    }

    public static void renderFluid(Fluid fluid) {
        TextureAtlasSprite sprite = PlatformSpecial.INSTANCE.get().getFluidSprite(fluid);
        Matrix4f backup = null;
        VertexSorting sorting = RenderSystem.getVertexSorting();
        try {
            backup = (Matrix4f) RenderSystem.getProjectionMatrix().clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        Matrix4f projection = new Matrix4f().setOrtho(0.0f, 16.0f, 0.0f, 16.0f, -150f, 150f);
        Matrix4f modelView = createTranslateMatrix(1.0e-4f, 1.0e-4f, 0.0f);
        projection.mul(modelView);
        RenderSystem.setProjectionMatrix(projection, sorting);
        renderFluidSprite(sprite);
        RenderSystem.setProjectionMatrix(backup, sorting);
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
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

        buffer.vertex(matrix, 0, 0, 0).color(255, 255, 255, 255).uv(u1, v0).endVertex();
        buffer.vertex(matrix, 0, 16, 0).color(255, 255, 255, 255).uv(u0, v0).endVertex();
        buffer.vertex(matrix, 16, 16, 0).color(255, 255, 255, 255).uv(u0, v1).endVertex();
        buffer.vertex(matrix, 16, 0, 0).color(255, 255, 255, 255).uv(u1, v1).endVertex();

        tesselator.end();
    }

    public static void renderGuiItem(Minecraft mc, ItemRenderer renderer, ItemStack stack, int i, int j) {
        BakedModel bakedModel = renderer.getModel(stack, null, null, 0);
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate((float) i + 8, (float) j + 8, 150f);
        try {
            poseStack.mulPoseMatrix(new Matrix4f().scaling(1.0f, -1.0f, 1.0f));
            poseStack.scale(16.0f, 16.0f, 16.0f);
            boolean flag = !bakedModel.usesBlockLight();
            if (flag) {
                Lighting.setupForFlatItems();
            }
            renderer.render(
                    stack, ItemDisplayContext.GUI,
                    false, poseStack, mc.renderBuffers().bufferSource(),
                    15728880, OverlayTexture.NO_OVERLAY,
                    bakedModel
            );
            if (flag) {
                Lighting.setupFor3DItems();
            }
        } catch (Throwable throwable) {
            ProbeJS.LOGGER.error("Error rendering item:", throwable);
        }
        poseStack.popPose();
    }
}
