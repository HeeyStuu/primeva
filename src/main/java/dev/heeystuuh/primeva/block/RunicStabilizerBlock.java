package dev.heeystuuh.primeva.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Estabilizador Rúnico: perto do Altar (até 6 blocos) segura a instabilidade dos rituais. Até 8 contam. */
public class RunicStabilizerBlock extends Block {
    public static final MapCodec<RunicStabilizerBlock> CODEC = simpleCodec(RunicStabilizerBlock::new);
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 14, 12);

    public RunicStabilizerBlock(Properties p) {
        super(p);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rnd) {
        if (rnd.nextInt(3) == 0) level.addParticle(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                (rnd.nextDouble() - 0.5), 0.3, (rnd.nextDouble() - 0.5));
    }
}
