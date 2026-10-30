package dev.heeystuuh.primeva.client;

import dev.heeystuuh.primeva.ModBlockEntities;
import dev.heeystuuh.primeva.PrimevaMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = PrimevaMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.PEDESTAL.get(), ctx -> new ItemHoverRenderer<>());
        event.registerBlockEntityRenderer(ModBlockEntities.ALTAR.get(), ctx -> new ItemHoverRenderer<>());
        event.registerBlockEntityRenderer(ModBlockEntities.CRUCIBLE.get(), ctx -> new CrucibleRenderer());
        event.registerBlockEntityRenderer(ModBlockEntities.ESSENCE_JAR.get(), ctx -> new JarRenderer());
        event.registerEntityRenderer(dev.heeystuuh.primeva.ModEntities.ARCANE_GOLEM.get(), GolemRenderer::new);
        event.registerEntityRenderer(dev.heeystuuh.primeva.ModEntities.CRIMSON_CULTIST.get(), CultistRenderer::new);
        event.registerEntityRenderer(dev.heeystuuh.primeva.ModEntities.CRIMSON_HERALD.get(), HeraldRenderer::new);
        event.registerEntityRenderer(dev.heeystuuh.primeva.ModEntities.MIASMA_WRAITH.get(), WraithRenderer::new);
    }

    @SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GolemModel.LAYER, GolemModel::create);
    }

    @SubscribeEvent
    public static void packs(net.neoforged.neoforge.event.AddPackFindersEvent event) {
        if (event.getPackType() != net.minecraft.server.packs.PackType.CLIENT_RESOURCES) return;
        event.addRepositorySource(consumer -> {
            net.minecraft.server.packs.repository.Pack pack = VanillaTintPack.create();
            if (pack != null) consumer.accept(pack);
        });
    }

    @SubscribeEvent
    public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.minecraft.world.item.Item bow = dev.heeystuuh.primeva.ModItems.ELDER_BOW.get();
            net.minecraft.client.renderer.item.ItemProperties.register(bow, net.minecraft.resources.ResourceLocation.withDefaultNamespace("pull"),
                    (stack, level, e, seed) -> e == null || e.getUseItem() != stack ? 0f : (stack.getUseDuration(e) - e.getUseItemRemainingTicks()) / 20f);
            net.minecraft.client.renderer.item.ItemProperties.register(bow, net.minecraft.resources.ResourceLocation.withDefaultNamespace("pulling"),
                    (stack, level, e, seed) -> e != null && e.isUsingItem() && e.getUseItem() == stack ? 1f : 0f);
        });
    }

    private ClientModEvents() {}
}
