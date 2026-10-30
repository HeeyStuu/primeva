package dev.heeystuuh.primeva.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Chama Eterna: uma luz que flutua no ar e nunca se apaga. */
public class EternalFlameBlock extends Block {
    public static final MapCodec<EternalFlameBlock> CODEC = simpleCodec(EternalFlameBlock::new);
    private static final VoxelShape SHAPE = Block.box(5, 5, 5, 11, 11, 11);

    public EternalFlameBlock(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rnd) {
        double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;
        level.addParticle(ParticleTypes.FLAME, x + (rnd.nextDouble() - 0.5) * 0.15, y + (rnd.nextDouble() - 0.5) * 0.15, z + (rnd.nextDouble() - 0.5) * 0.15, 0, 0.01, 0);
        level.addParticle(ParticleTypes.SMALL_FLAME, x, y + 0.1, z, 0, 0.015, 0);
        if (rnd.nextInt(4) == 0) level.addParticle(ParticleTypes.END_ROD, x, y, z, (rnd.nextDouble() - 0.5) * 0.02, 0.02, (rnd.nextDouble() - 0.5) * 0.02);
    }
}
