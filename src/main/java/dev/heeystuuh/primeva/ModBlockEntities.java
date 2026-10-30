package dev.heeystuuh.primeva;

import dev.heeystuuh.primeva.block.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PrimevaMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DistillerBlockEntity>> DISTILLER =
            BLOCK_ENTITIES.register("distiller", () -> BlockEntityType.Builder.of(DistillerBlockEntity::new, ModBlocks.DISTILLER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PedestalBlockEntity>> PEDESTAL =
            BLOCK_ENTITIES.register("pedestal", () -> BlockEntityType.Builder.of(PedestalBlockEntity::new, ModBlocks.PEDESTAL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AltarBlockEntity>> ALTAR =
            BLOCK_ENTITIES.register("altar", () -> BlockEntityType.Builder.of(AltarBlockEntity::new, ModBlocks.ALTAR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PurifierBlockEntity>> PURIFIER =
            BLOCK_ENTITIES.register("purifier", () -> BlockEntityType.Builder.of(PurifierBlockEntity::new, ModBlocks.PURIFIER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrucibleBlockEntity>> CRUCIBLE =
            BLOCK_ENTITIES.register("crucible", () -> BlockEntityType.Builder.of(CrucibleBlockEntity::new, ModBlocks.CRUCIBLE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HungryChestBlockEntity>> HUNGRY_CHEST =
            BLOCK_ENTITIES.register("hungry_chest", () -> BlockEntityType.Builder.of(HungryChestBlockEntity::new, ModBlocks.HUNGRY_CHEST.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcaneLampBlockEntity>> ARCANE_LAMP =
            BLOCK_ENTITIES.register("arcane_lamp", () -> BlockEntityType.Builder.of(ArcaneLampBlockEntity::new, ModBlocks.ARCANE_LAMP.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EssenceJarBlockEntity>> ESSENCE_JAR =
            BLOCK_ENTITIES.register("essence_jar", () -> BlockEntityType.Builder.of(EssenceJarBlockEntity::new, ModBlocks.ESSENCE_JAR.get()).build(null));

    private ModBlockEntities() {}
}
