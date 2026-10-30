package dev.heeystuuh.primeva.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.heeystuuh.primeva.block.EssenceJarBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix4f;

/** Desenha o líquido colorido dentro do Jarro de Essência. */
public class JarRenderer implements BlockEntityRenderer<EssenceJarBlockEntity> {
    private static final ResourceLocation WATER = ResourceLocation.withDefaultNamespace("block/water_still");

    @Override
    public void render(EssenceJarBlockEntity be, float pt, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (be.essence() == null || be.amount() <= 0) return;
        TextureAtlasSprite sp = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(WATER);
        int c = be.essence().rgb;
        float r = ((c >> 16) & 255) / 255f, g = ((c >> 8) & 255) / 255f, b = (c & 255) / 255f;
        float lo = 3.6f / 16f, hi = 12.4f / 16f, y0 = 0.6f / 16f;
        float y1 = y0 + (11f / 16f) * Math.min(1f, be.amount() / (float) EssenceJarBlockEntity.capacity());
        VertexConsumer vc = buffers.getBuffer(RenderType.translucent());
        PoseStack.Pose last = pose.last();
        Matrix4f m = last.pose();
        int li = 0xF000F0;
        float u0 = sp.getU(0.2f), u1 = sp.getU(0.8f), v0 = sp.getV(0.2f), v1 = sp.getV(0.8f);
        float a = 0.8f;
        // topo
        q(vc, m, last, lo, y1, lo, lo, y1, hi, hi, y1, hi, hi, y1, lo, r, g, b, a, u0, u1, v0, v1, li, 0, 1, 0);
        // lados
        q(vc, m, last, lo, y0, lo, lo, y1, lo, hi, y1, lo, hi, y0, lo, r, g, b, a, u0, u1, v0, v1, li, 0, 0, -1);
        q(vc, m, last, hi, y0, hi, hi, y1, hi, lo, y1, hi, lo, y0, hi, r, g, b, a, u0, u1, v0, v1, li, 0, 0, 1);
        q(vc, m, last, lo, y0, hi, lo, y1, hi, lo, y1, lo, lo, y0, lo, r, g, b, a, u0, u1, v0, v1, li, -1, 0, 0);
        q(vc, m, last, hi, y0, lo, hi, y1, lo, hi, y1, hi, hi, y0, hi, r, g, b, a, u0, u1, v0, v1, li, 1, 0, 0);
    }

    private static void q(VertexConsumer vc, Matrix4f m, PoseStack.Pose p, float x1, float y1, float z1, float x2, float y2, float z2,
                          float x3, float y3, float z3, float x4, float y4, float z4, float r, float g, float b, float a,
                          float u0, float u1, float v0, float v1, int li, float nx, float ny, float nz) {
        vc.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setUv(u0, v0).setLight(li).setNormal(p, nx, ny, nz);
        vc.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setUv(u0, v1).setLight(li).setNormal(p, nx, ny, nz);
        vc.addVertex(m, x3, y3, z3).setColor(r, g, b, a).setUv(u1, v1).setLight(li).setNormal(p, nx, ny, nz);
        vc.addVertex(m, x4, y4, z4).setColor(r, g, b, a).setUv(u1, v0).setLight(li).setNormal(p, nx, ny, nz);
    }
}
