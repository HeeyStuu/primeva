package dev.heeystuuh.primeva;

import dev.heeystuuh.primeva.world.CorruptionSiteFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, PrimevaMod.MODID);

    public static final DeferredHolder<Feature<?>, CorruptionSiteFeature> CORRUPTION_SITE =
            FEATURES.register("corruption_site", () -> new CorruptionSiteFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {}
}
