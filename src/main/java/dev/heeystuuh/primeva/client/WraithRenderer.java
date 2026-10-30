package dev.heeystuuh.primeva.client;

import dev.heeystuuh.primeva.PrimevaMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VexRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vex;

public class WraithRenderer extends VexRenderer {
    private static final ResourceLocation TEX = PrimevaMod.id("textures/entity/miasma_wraith.png");

    public WraithRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(Vex e) {
        return TEX;
    }
}
