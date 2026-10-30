package dev.heeystuuh.primeva.client;

import dev.heeystuuh.primeva.PrimevaMod;
import dev.heeystuuh.primeva.entity.ArcaneGolem;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GolemRenderer extends MobRenderer<ArcaneGolem, GolemModel> {
    private static final ResourceLocation TEX = PrimevaMod.id("textures/entity/arcane_golem.png");

    public GolemRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new GolemModel(ctx.bakeLayer(GolemModel.LAYER)), 0.5f);
        addLayer(new net.minecraft.client.renderer.entity.layers.ItemInHandLayer<>(this, ctx.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(ArcaneGolem e) {
        return TEX;
    }
}
