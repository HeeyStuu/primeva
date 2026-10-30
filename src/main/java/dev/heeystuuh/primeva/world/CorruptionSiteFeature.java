package dev.heeystuuh.primeva.world;

import com.mojang.serialization.Codec;
import dev.heeystuuh.primeva.ModBlocks;
import dev.heeystuuh.primeva.PrimevaMod;
import dev.heeystuuh.primeva.block.CorruptedSoilBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Foco de Corrupção: um círculo de solo miasmático enraizado, cristais de miasma,
 * um monólito escuro e às vezes um baú enterrado com espólio.
 */
public class CorruptionSiteFeature extends Feature<NoneFeatureConfiguration> {
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, PrimevaMod.id("chests/corruption_site"));

    public CorruptionSiteFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource r = ctx.random();
        BlockPos origin = ctx.origin();
        int radius = 4 + r.nextInt(3);
        BlockState soil = ModBlocks.CORRUPTED_SOIL.get().defaultBlockState().setValue(CorruptedSoilBlock.ROOTED, true);
        int placed = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius + r.nextInt(3)) continue;
                int x = origin.getX() + dx, z = origin.getZ() + dz;
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                BlockPos p = new BlockPos(x, y, z);
                BlockState st = level.getBlockState(p);
                if (!st.is(BlockTags.DIRT)) continue;
                level.setBlock(p, soil, 2);
                placed++;
                BlockPos up = p.above();
                BlockState above = level.getBlockState(up);
                if (above.is(Blocks.SHORT_GRASS) || above.is(Blocks.TALL_GRASS) || above.is(BlockTags.SMALL_FLOWERS)) {
                    level.setBlock(up, Blocks.DEAD_BUSH.defaultBlockState(), 2);
                } else if (above.isAir() && r.nextInt(7) == 0) {
                    level.setBlock(up, ModBlocks.MIASMA_CRYSTAL.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP), 2);
                }
            }
        }
        if (placed < 6) return false;
        // monólito no centro
        int cy = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        BlockPos base = new BlockPos(origin.getX(), cy, origin.getZ());
        int h = 2 + r.nextInt(3);
        for (int i = 0; i < h; i++) level.setBlock(base.above(i), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
        level.setBlock(base.above(h), Blocks.CRYING_OBSIDIAN.defaultBlockState(), 2);
        level.setBlock(base.above(h + 1), ModBlocks.MIASMA_CRYSTAL.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP), 2);
        // baú enterrado embaixo do monólito
        if (r.nextInt(3) != 0) {
            BlockPos chest = base.below(2);
            level.setBlock(chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(r)), 2);
            RandomizableContainer.setBlockEntityLootTable(level, r, chest, LOOT);
        }
        return true;
    }
}
