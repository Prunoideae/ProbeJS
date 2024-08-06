package moe.wolfgirl.probejs.utils;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ImageUtils {
    // We ensure that only one item is rendered no matter how many threads are using it,
    // since RenderSystem is probably global
    private static final Lock LOCK = new ReentrantLock();

    /**
     * A simple implementation to render item, since Lat will probably add one later.
     * <p>
     * Or in case if he doesn't, I can add the remaining stuffs after extension is done...
     */
    public static NativeImage renderItem(ItemStack itemStack, int width, int height) {
        LOCK.lock();

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return null;

        var image = new NativeImage(width, height, false);
        minecraft.executeBlocking(() -> {
            var bufferSource = minecraft.renderBuffers().bufferSource();
            var renderTarget = new TextureTarget(width, height, true, false);

            renderTarget.bindWrite(true);
            Matrix4f matrix = new Matrix4f().setOrtho(
                    0, 16, 16, 0,
                    1000, 21000
            );
            RenderSystem.setProjectionMatrix(matrix, VertexSorting.ORTHOGRAPHIC_Z);

            Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
            matrix4fStack.pushMatrix();
            matrix4fStack.translation(0, 0, -11000);

            RenderSystem.applyModelViewMatrix();

            GuiGraphics graphics = new GuiGraphics(minecraft, bufferSource);
            graphics.renderItem(minecraft.player, itemStack, 0, 0, 0);
            graphics.renderItemDecorations(minecraft.font, itemStack, 0, 0);
            graphics.flush();

            renderTarget.bindRead();
            RenderSystem.bindTexture(renderTarget.getColorTextureId());

            image.downloadTexture(0, false);
            image.flipY();
            renderTarget.unbindRead();
            renderTarget.unbindWrite();

            matrix4fStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
        });

        LOCK.unlock();
        return image;
    }
}
