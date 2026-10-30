package dev.heeystuuh.primeva.block;

import dev.heeystuuh.primeva.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class ArcaneLampBlockEntity extends BlockEntity {
    public static final int MAX = 64;
    private final List<Long> lights = new ArrayList<>();

    public ArcaneLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_LAMP.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneLampBlockEntity be) {
        int RADIUS = dev.heeystuuh.primeva.Config.get(dev.heeystuuh.primeva.Config.LAMP_RADIUS);
        if (level.getGameTime() % 10 != 0 || be.lights.size() >= MAX) return;
        BlockPos p = pos.offset(level.random.nextInt(RADIUS * 2 + 1) - RADIUS, level.random.nextInt(9) - 4, level.random.nextInt(RADIUS * 2 + 1) - RADIUS);
        if (!level.isLoaded(p) || !level.getBlockState(p).isAir()) return;
        if (level.getBrightness(LightLayer.BLOCK, p) > 8) return;
        if (!level.getBlockState(p.below()).isFaceSturdy(level, p.below(), net.minecraft.core.Direction.UP)) return;
        level.setBlock(p, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 13), 3);
        be.lights.add(p.asLong());
        be.setChanged();
    }

    public void clearLights() {
        if (level == null) return;
        for (long l : lights) {
            BlockPos p = BlockPos.of(l);
            if (level.getBlockState(p).is(Blocks.LIGHT)) level.removeBlock(p, false);
        }
        lights.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("lights", new LongArrayTag(lights));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        lights.clear();
        for (long l : tag.getLongArray("lights")) lights.add(l);
    }
}
