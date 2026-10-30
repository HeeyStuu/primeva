package dev.heeystuuh.primeva.client;

import dev.heeystuuh.primeva.PrimevaMod;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vindicator;

/** Cultista Carmesim: manto quase preto, rosto na sombra, olhos e runas que brilham no escuro e a foice sempre na mão. */
public class CultistRenderer extends IllagerRenderer<Vindicator> {
    private static final ResourceLocation TEX = PrimevaMod.id("textures/entity/crimson_cultist.png");
    private static final RenderType EYES = RenderType.eyes(PrimevaMod.id("textures/entity/crimson_cultist_eyes.png"));

    public CultistRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new IllagerModel<>(ctx.bakeLayer(ModelLayers.VINDICATOR)), 0.5f);
        addLayer(new ItemInHandLayer<>(this, ctx.getItemInHandRenderer()));
        addLayer(new EyesLayer<Vindicator, IllagerModel<Vindicator>>(this) {
            @Override
            public RenderType renderType() {
                return EYES;
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(Vindicator e) {
        return TEX;
    }
}
