package dev.heeystuuh.primeva.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.heeystuuh.primeva.PrimevaMod;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EvokerRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Evoker;

/** Arauto Carmesim: maior que um illager, com coroa dourada e olhos que brilham. */
public class HeraldRenderer extends EvokerRenderer<Evoker> {
    private static final ResourceLocation TEX = PrimevaMod.id("textures/entity/crimson_herald.png");
    private static final RenderType EYES = RenderType.eyes(PrimevaMod.id("textures/entity/crimson_herald_eyes.png"));

    public HeraldRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        addLayer(new EyesLayer<Evoker, IllagerModel<Evoker>>(this) {
            @Override
            public RenderType renderType() {
                return EYES;
            }
        });
    }

    @Override
    protected void scale(Evoker e, PoseStack pose, float pt) {
        pose.scale(1.25f, 1.25f, 1.25f);
    }

    @Override
    public ResourceLocation getTextureLocation(Evoker e) {
        return TEX;
    }
}
