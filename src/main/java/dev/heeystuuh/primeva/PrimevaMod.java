package dev.heeystuuh.primeva;

import dev.heeystuuh.primeva.network.Network;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(PrimevaMod.MODID)
public class PrimevaMod {
    public static final String MODID = "primeva";

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public PrimevaMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModArmor.ARMOR.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModFeatures.FEATURES.register(modBus);
        ModLoot.MODIFIERS.register(modBus);
        modBus.addListener(ModEntities::attributes);
        modBus.addListener(ModEntities::spawns);
        ModRecipes.TYPES.register(modBus);
        ModRecipes.SERIALIZERS.register(modBus);
        ModAttachments.ATTACHMENTS.register(modBus);
        ModTabs.TABS.register(modBus);
        modBus.addListener(ModDataMaps::register);
        modBus.addListener(Network::register);
        NeoForge.EVENT_BUS.register(CommonEvents.class);
        NeoForge.EVENT_BUS.register(Miasma.class);
        NeoForge.EVENT_BUS.register(PortableHoles.class);
        NeoForge.EVENT_BUS.register(Discovery.class);
        NeoForge.EVENT_BUS.register(Warp.class);
        NeoForge.EVENT_BUS.register(dev.heeystuuh.primeva.block.NewBlocks.class);
    }
}
