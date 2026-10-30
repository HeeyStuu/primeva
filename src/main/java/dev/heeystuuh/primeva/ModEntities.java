package dev.heeystuuh.primeva;

import dev.heeystuuh.primeva.entity.ArcaneGolem;
import dev.heeystuuh.primeva.entity.CrimsonCultist;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, PrimevaMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ArcaneGolem>> ARCANE_GOLEM = ENTITIES.register("arcane_golem",
            () -> EntityType.Builder.<ArcaneGolem>of(ArcaneGolem::new, MobCategory.MISC).sized(0.8f, 1.5f).clientTrackingRange(10)
                    .build(PrimevaMod.MODID + ":arcane_golem"));
    public static final DeferredHolder<EntityType<?>, EntityType<CrimsonCultist>> CRIMSON_CULTIST = ENTITIES.register("crimson_cultist",
            () -> EntityType.Builder.<CrimsonCultist>of(CrimsonCultist::new, MobCategory.MONSTER).sized(0.6f, 1.95f).clientTrackingRange(8)
                    .build(PrimevaMod.MODID + ":crimson_cultist"));

    public static final DeferredHolder<EntityType<?>, EntityType<dev.heeystuuh.primeva.entity.CrimsonHerald>> CRIMSON_HERALD = ENTITIES.register("crimson_herald",
            () -> EntityType.Builder.<dev.heeystuuh.primeva.entity.CrimsonHerald>of(dev.heeystuuh.primeva.entity.CrimsonHerald::new, MobCategory.MONSTER)
                    .sized(0.75f, 2.4f).clientTrackingRange(10).fireImmune().build(PrimevaMod.MODID + ":crimson_herald"));

    public static final DeferredHolder<EntityType<?>, EntityType<dev.heeystuuh.primeva.entity.MiasmaWraith>> MIASMA_WRAITH = ENTITIES.register("miasma_wraith",
            () -> EntityType.Builder.<dev.heeystuuh.primeva.entity.MiasmaWraith>of(dev.heeystuuh.primeva.entity.MiasmaWraith::new, MobCategory.MONSTER)
                    .sized(0.5f, 0.9f).clientTrackingRange(8).fireImmune().build(PrimevaMod.MODID + ":miasma_wraith"));

    public static void attributes(EntityAttributeCreationEvent e) {
        e.put(MIASMA_WRAITH.get(), dev.heeystuuh.primeva.entity.MiasmaWraith.createWraithAttributes().build());
        e.put(CRIMSON_HERALD.get(), dev.heeystuuh.primeva.entity.CrimsonHerald.createHeraldAttributes().build());
        e.put(ARCANE_GOLEM.get(), ArcaneGolem.createAttributes().build());
        e.put(CRIMSON_CULTIST.get(), CrimsonCultist.createCultistAttributes().build());
    }

    public static void spawns(RegisterSpawnPlacementsEvent e) {
        e.register(CRIMSON_CULTIST.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, lvl, reason, pos, rnd) -> Monster.checkMonsterSpawnRules(type, lvl, reason, pos, rnd)
                        && (reason != net.minecraft.world.entity.MobSpawnType.NATURAL || rnd.nextDouble() < Config.get(Config.CULTIST_SPAWN)),
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private ModEntities() {}
}
