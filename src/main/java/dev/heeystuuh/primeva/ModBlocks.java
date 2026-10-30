package dev.heeystuuh.primeva;

import dev.heeystuuh.primeva.block.*;
import dev.heeystuuh.primeva.block.CrucibleBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.PushReaction;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PrimevaMod.MODID);

    public static final DeferredBlock<DistillerBlock> DISTILLER = BLOCKS.register("distiller",
            () -> new DistillerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3f, 6f)
                    .sound(SoundType.COPPER).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<PedestalBlock> PEDESTAL = BLOCKS.register("pedestal",
            () -> new PedestalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2f, 6f)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<AltarBlock> ALTAR = BLOCKS.register("altar",
            () -> new AltarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(3f, 9f)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops().noOcclusion().lightLevel(s -> s.getValue(AltarBlock.ACTIVE) ? 15 : 7)));

    public static final DeferredBlock<PurifierBlock> PURIFIER = BLOCKS.register("purifier",
            () -> new PurifierBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(2f, 6f)
                    .sound(SoundType.LANTERN).lightLevel(s -> 15).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<CorruptedSoilBlock> CORRUPTED_SOIL = BLOCKS.register("corrupted_soil",
            () -> new CorruptedSoilBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.6f)
                    .sound(SoundType.ROOTED_DIRT).randomTicks()));

    public static final DeferredBlock<ContainmentPillarBlock> CONTAINMENT_PILLAR = BLOCKS.register("containment_pillar",
            () -> new ContainmentPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(25f, 1200f)
                    .sound(SoundType.DEEPSLATE_BRICKS).requiresCorrectToolForDrops().noOcclusion().lightLevel(s -> 5)));

    public static final DeferredBlock<CatalystBlock> CATALYST = BLOCKS.register("catalyst",
            () -> new CatalystBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).strength(50f, 1200f)
                    .sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops().lightLevel(s -> 12)));

    public static final DeferredBlock<StudyTableBlock> STUDY_TABLE = BLOCKS.register("study_table",
            () -> new StudyTableBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5f)
                    .sound(SoundType.WOOD).noOcclusion()));

    // ================= Alquimia (inspirado no Thaumcraft) =================
    public static final DeferredBlock<DropExperienceBlock> VERMILION_ORE = BLOCKS.register("vermilion_ore",
            () -> new DropExperienceBlock(UniformInt.of(1, 3), BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_VERMILION_ORE = BLOCKS.register("deepslate_vermilion_ore",
            () -> new DropExperienceBlock(UniformInt.of(1, 3), BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)));
    public static final DeferredBlock<DropExperienceBlock> AMBER_ORE = BLOCKS.register("amber_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 5), BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_ORE)));

    public static final Map<Essence, DeferredBlock<AmethystClusterBlock>> CLUSTERS = new EnumMap<>(Essence.class);
    static {
        for (Essence e : Essence.values()) {
            CLUSTERS.put(e, BLOCKS.register("crystal_cluster_" + e.id, () -> new AmethystClusterBlock(7.0f, 3.0f,
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).forceSolidOn().noOcclusion()
                            .sound(SoundType.AMETHYST_CLUSTER).strength(1.5f).lightLevel(s -> 6).pushReaction(PushReaction.DESTROY))));
        }
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> tree(String id) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, PrimevaMod.id(id));
    }
    public static final TreeGrower ARGENT_GROWER = new TreeGrower("primeva_argent", Optional.empty(), Optional.of(tree("argent_tree")), Optional.empty());
    public static final TreeGrower ELDER_GROWER = new TreeGrower("primeva_elder", Optional.empty(), Optional.of(tree("elder_tree")), Optional.empty());

    public static final DeferredBlock<RotatedPillarBlock> ARGENT_LOG = BLOCKS.register("argent_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_LOG).mapColor(MapColor.QUARTZ).lightLevel(s -> 3)));
    public static final DeferredBlock<Block> ARGENT_PLANKS = BLOCKS.register("argent_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_PLANKS).mapColor(MapColor.QUARTZ)));
    public static final DeferredBlock<LeavesBlock> ARGENT_LEAVES = BLOCKS.register("argent_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_LEAVES).mapColor(MapColor.COLOR_LIGHT_BLUE).lightLevel(s -> 4)));
    public static final DeferredBlock<SaplingBlock> ARGENT_SAPLING = BLOCKS.register("argent_sapling",
            () -> new SaplingBlock(ARGENT_GROWER, BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_SAPLING)));
    public static final DeferredBlock<RotatedPillarBlock> ELDER_LOG = BLOCKS.register("elder_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_LOG)));
    public static final DeferredBlock<Block> ELDER_PLANKS = BLOCKS.register("elder_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_PLANKS)));
    public static final DeferredBlock<LeavesBlock> ELDER_LEAVES = BLOCKS.register("elder_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_LEAVES)));
    public static final DeferredBlock<SaplingBlock> ELDER_SAPLING = BLOCKS.register("elder_sapling",
            () -> new SaplingBlock(ELDER_GROWER, BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_SAPLING)));

    public static final DeferredBlock<Block> ARCANITE_BLOCK = BLOCKS.register("arcanite_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).mapColor(MapColor.COLOR_PURPLE)));

    public static final DeferredBlock<CrucibleBlock> CRUCIBLE = BLOCKS.register("crucible",
            () -> new CrucibleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON).mapColor(MapColor.COLOR_BLACK).noOcclusion()));
    public static final DeferredBlock<HungryChestBlock> HUNGRY_CHEST = BLOCKS.register("hungry_chest",
            () -> new HungryChestBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST)));
    public static final DeferredBlock<ArcaneLampBlock> ARCANE_LAMP = BLOCKS.register("arcane_lamp",
            () -> new ArcaneLampBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(1.5f)
                    .sound(SoundType.LANTERN).lightLevel(s -> 15).requiresCorrectToolForDrops()));
    public static final DeferredBlock<EternalFlameBlock> ETERNAL_FLAME = BLOCKS.register("eternal_flame",
            () -> new EternalFlameBlock(BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).instabreak().noCollission()
                    .noOcclusion().lightLevel(s -> 15).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<EssenceJarBlock> ESSENCE_JAR = BLOCKS.register("essence_jar",
            () -> new EssenceJarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.6f).sound(SoundType.GLASS).noOcclusion()));
    public static final DeferredBlock<EssenceTubeBlock> ESSENCE_TUBE = BLOCKS.register("essence_tube",
            () -> new EssenceTubeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(1f).sound(SoundType.COPPER).noOcclusion()));

    public static final DeferredBlock<AmethystClusterBlock> MIASMA_CRYSTAL = BLOCKS.register("miasma_crystal",
            () -> new AmethystClusterBlock(6.0f, 3.0f, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).forceSolidOn().noOcclusion()
                    .sound(SoundType.AMETHYST_CLUSTER).strength(1.2f).lightLevel(s -> 4).pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<EssenceFilterTubeBlock> ESSENCE_FILTER_TUBE = BLOCKS.register("essence_filter_tube",
            () -> new EssenceFilterTubeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(1f).sound(SoundType.COPPER).noOcclusion()));

    public static final DeferredBlock<RunicStabilizerBlock> RUNIC_STABILIZER = BLOCKS.register("runic_stabilizer",
            () -> new RunicStabilizerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(2f, 6f)
                    .sound(SoundType.AMETHYST).requiresCorrectToolForDrops().noOcclusion().lightLevel(s -> 8)));

    // ---------------- novidades ----------------
    private static BlockBehaviour.Properties lamp() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(1.5f).sound(SoundType.LANTERN).lightLevel(s -> 12).requiresCorrectToolForDrops();
    }
    public static final DeferredBlock<dev.heeystuuh.primeva.block.NewBlocks.GrowthLamp> GROWTH_LAMP = BLOCKS.register("growth_lamp",
            () -> new dev.heeystuuh.primeva.block.NewBlocks.GrowthLamp(lamp()));
    public static final DeferredBlock<dev.heeystuuh.primeva.block.NewBlocks.FertilityLamp> FERTILITY_LAMP = BLOCKS.register("fertility_lamp",
            () -> new dev.heeystuuh.primeva.block.NewBlocks.FertilityLamp(lamp().mapColor(MapColor.COLOR_PINK)));
    public static final DeferredBlock<dev.heeystuuh.primeva.block.NewBlocks.Levitator> LEVITATOR = BLOCKS.register("levitator",
            () -> new dev.heeystuuh.primeva.block.NewBlocks.Levitator(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(2f, 6f).sound(SoundType.COPPER).requiresCorrectToolForDrops()));
    public static final DeferredBlock<dev.heeystuuh.primeva.block.NewBlocks.WardingStone> WARDING_STONE = BLOCKS.register("warding_stone",
            () -> new dev.heeystuuh.primeva.block.NewBlocks.WardingStone(BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ)
                    .strength(3f, 9f).sound(SoundType.STONE).requiresCorrectToolForDrops().lightLevel(s -> 6)));
    public static final DeferredBlock<Block> ARCANE_STONE = BLOCKS.register("arcane_stone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<Block> ARCANE_BRICKS = BLOCKS.register("arcane_bricks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<StairBlock> ARCANE_BRICK_STAIRS = BLOCKS.register("arcane_brick_stairs",
            () -> new StairBlock(Blocks.STONE_BRICKS.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_STAIRS).mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<SlabBlock> ARCANE_BRICK_SLAB = BLOCKS.register("arcane_brick_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_SLAB).mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<StairBlock> ELDER_STAIRS = BLOCKS.register("elder_stairs",
            () -> new StairBlock(Blocks.DARK_OAK_PLANKS.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_STAIRS)));
    public static final DeferredBlock<SlabBlock> ELDER_SLAB = BLOCKS.register("elder_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_OAK_SLAB)));
    public static final DeferredBlock<StairBlock> ARGENT_STAIRS = BLOCKS.register("argent_stairs",
            () -> new StairBlock(Blocks.BIRCH_PLANKS.defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_STAIRS).mapColor(MapColor.QUARTZ)));
    public static final DeferredBlock<SlabBlock> ARGENT_SLAB = BLOCKS.register("argent_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_SLAB).mapColor(MapColor.QUARTZ)));

    private ModBlocks() {}
}
