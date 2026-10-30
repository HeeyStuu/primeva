package dev.heeystuuh.primeva.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.heeystuuh.primeva.block.CrucibleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix4f;

/** Superfície do líquido do Cadinho, colorida pela mistura de essências. */
public class CrucibleRenderer implements BlockEntityRenderer<CrucibleBlockEntity> {
    private static final ResourceLocation WATER = ResourceLocation.withDefaultNamespace("block/water_still");

    @Override
    public void render(CrucibleBlockEntity be, float pt, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (!be.hasWater()) return;
        TextureAtlasSprite sp = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(WATER);
        int c = be.color();
        float r = ((c >> 16) & 255) / 255f, g = ((c >> 8) & 255) / 255f, b = (c & 255) / 255f;
        float y = 0.8125f + Math.min(1f, be.total() / (float) CrucibleBlockEntity.capacity()) * 0.09f;
        float lo = 2 / 16f, hi = 14 / 16f;
        VertexConsumer vc = buffers.getBuffer(RenderType.translucent());
        Matrix4f m = pose.last().pose();
        int full = 0xF000F0;
        int li = be.total() > 0 ? full : light;
        vc.addVertex(m, lo, y, lo).setColor(r, g, b, 0.85f).setUv(sp.getU(0.125f), sp.getV(0.125f)).setLight(li).setNormal(pose.last(), 0, 1, 0);
        vc.addVertex(m, lo, y, hi).setColor(r, g, b, 0.85f).setUv(sp.getU(0.125f), sp.getV(0.875f)).setLight(li).setNormal(pose.last(), 0, 1, 0);
        vc.addVertex(m, hi, y, hi).setColor(r, g, b, 0.85f).setUv(sp.getU(0.875f), sp.getV(0.875f)).setLight(li).setNormal(pose.last(), 0, 1, 0);
        vc.addVertex(m, hi, y, lo).setColor(r, g, b, 0.85f).setUv(sp.getU(0.875f), sp.getV(0.125f)).setLight(li).setNormal(pose.last(), 0, 1, 0);
    }
}
