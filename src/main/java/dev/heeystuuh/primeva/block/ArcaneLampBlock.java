package dev.heeystuuh.primeva.block;

import com.mojang.serialization.MapCodec;
import dev.heeystuuh.primeva.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

/** Lâmpada Arcana: espalha luzes invisíveis nos lugares escuros num raio de 14 blocos. */
public class ArcaneLampBlock extends BaseEntityBlock {
    public static final MapCodec<ArcaneLampBlock> CODEC = simpleCodec(ArcaneLampBlock::new);

    public ArcaneLampBlock(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneLampBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.ARCANE_LAMP.get(), ArcaneLampBlockEntity::serverTick);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ArcaneLampBlockEntity be) be.clearLights();
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rnd) {
        if (rnd.nextInt(3) == 0) level.addParticle(new DustParticleOptions(new Vector3f(0.7f, 0.4f, 1f), 0.8f),
                pos.getX() + rnd.nextDouble(), pos.getY() + 1.05, pos.getZ() + rnd.nextDouble(), 0, 0.02, 0);
    }
}
