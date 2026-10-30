package dev.heeystuuh.primeva.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/** Tubo de Essência: liga Destiladores a Jarros. A essência destilada desce sozinha pelos tubos até os jarros. */
public class EssenceTubeBlock extends PipeBlock {
    public static final MapCodec<EssenceTubeBlock> CODEC = simpleCodec(EssenceTubeBlock::new);

    public EssenceTubeBlock(Properties props) {
        super(0.1875f, props);
        BlockState s = defaultBlockState();
        for (Direction d : Direction.values()) s = s.setValue(PROPERTY_BY_DIRECTION.get(d), false);
        registerDefaultState(s);
    }

    @Override
    protected MapCodec<? extends PipeBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    public static boolean connects(BlockGetter level, BlockPos pos) {
        BlockState st = level.getBlockState(pos);
        return st.getBlock() instanceof EssenceTubeBlock || st.getBlock() instanceof EssenceJarBlock || st.getBlock() instanceof DistillerBlock
                || st.getBlock() instanceof CrucibleBlock;
    }

    private BlockState compute(BlockGetter level, BlockPos pos, BlockState s) {
        for (Direction d : Direction.values()) s = s.setValue(PROPERTY_BY_DIRECTION.get(d), connects(level, pos.relative(d)));
        return s;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return compute(ctx.getLevel(), ctx.getClickedPos(), defaultBlockState());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(PROPERTY_BY_DIRECTION.get(dir), connects(level, neighborPos));
    }
}
