package dev.heeystuuh.primeva.block;

import com.mojang.serialization.MapCodec;
import dev.heeystuuh.primeva.ModItems;
import dev.heeystuuh.primeva.item.PhialItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Jarro de Essência: guarda um único tipo de essência. Frascos entram e saem com clique direito. */
public class EssenceJarBlock extends BaseEntityBlock {
    public static final MapCodec<EssenceJarBlock> CODEC = simpleCodec(EssenceJarBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(Block.box(3, 0, 3, 13, 12, 13), Block.box(4, 12, 4, 12, 14, 12));

    public EssenceJarBlock(Properties props) {
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
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EssenceJarBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        boolean phial = stack.getItem() instanceof PhialItem || stack.is(ModItems.EMPTY_PHIAL.get());
        if (!phial) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof EssenceJarBlockEntity be) {
            if (stack.getItem() instanceof PhialItem p) be.pour(player, stack, p.essence);
            else be.fill(player, stack);
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof EssenceJarBlockEntity be) {
            if (player.isShiftKeyDown()) be.vent(player);
            else be.report(player);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof EssenceJarBlockEntity be ? be.signal() : 0;
    }
}
