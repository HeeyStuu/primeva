package dev.heeystuuh.primeva.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.heeystuuh.primeva.block.DisplayItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Desenha o item do pedestal/altar girando e flutuando. */
public class ItemHoverRenderer<T extends BlockEntity & DisplayItem> implements BlockEntityRenderer<T> {
    @Override
    public void render(T be, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ItemStack stack = be.displayItem();
        if (stack.isEmpty() || be.getLevel() == null) return;
        float time = be.getLevel().getGameTime() + partialTick;
        pose.pushPose();
        pose.translate(0.5, be.displayHeight() + Math.sin(time / 10.0) * 0.06, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(time * 2f));
        pose.scale(0.7f, 0.7f, 0.7f);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, light,
                OverlayTexture.NO_OVERLAY, pose, buffers, be.getLevel(), (int) be.getBlockPos().asLong());
        pose.popPose();
    }
}
