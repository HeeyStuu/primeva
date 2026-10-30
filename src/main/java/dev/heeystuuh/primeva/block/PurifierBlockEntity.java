package dev.heeystuuh.primeva.block;

import dev.heeystuuh.primeva.Miasma;
import dev.heeystuuh.primeva.ModBlockEntities;
import dev.heeystuuh.primeva.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

/** Farol Purificador: a cada 5 s tira 1 de miasma do seu chunk e dos 8 vizinhos e cura o solo corrompido. */
public class PurifierBlockEntity extends BlockEntity {
    /** Ticks de reforço restantes (frasco de Lume): limpa 3x mais rápido. */
    private int boost;

    public PurifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PURIFIER.get(), pos, state);
    }

    public int boost() {
        return boost;
    }

    public void addBoost(int ticks) {
        boost = Math.min(boost + ticks, 20 * 60 * 20);
        setChanged();
    }

    @Override
    protected void saveAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider r) {
        super.saveAdditional(tag, r);
        tag.putInt("boost", boost);
    }

    @Override
    protected void loadAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider r) {
        super.loadAdditional(tag, r);
        boost = tag.getInt("boost");
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PurifierBlockEntity be) {
        if (be.boost > 0) {
            be.boost--;
            if (be.boost % 20 == 0) be.setChanged();
        }
        if (!(level instanceof ServerLevel sl) || level.getGameTime() % 100 != 0) return;
        int power = be.boost > 0 ? 3 : 1;
        int cx = pos.getX() >> 4, cz = pos.getZ() >> 4;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (!sl.hasChunk(cx + dx, cz + dz)) continue;
                LevelChunk chunk = sl.getChunk(cx + dx, cz + dz);
                Miasma.addToChunk(chunk, -power);
            }
        }
        for (int i = 0; i < 6 * power; i++) {
            BlockPos p = pos.offset(sl.random.nextInt(17) - 8, sl.random.nextInt(9) - 4, sl.random.nextInt(17) - 8);
            if (sl.getBlockState(p).is(ModBlocks.CORRUPTED_SOIL.get())) {
                CorruptedSoilBlock.purify(sl, p);
            } else if (sl.getBlockState(p).is(ModBlocks.MIASMA_CRYSTAL.get()) && power > 1) {
                sl.destroyBlock(p, true);
            }
        }
        sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 6, 0.2, 0.4, 0.2, 0.02);
    }
}
