package dev.heeystuuh.primeva.block;

import com.mojang.serialization.MapCodec;
import dev.heeystuuh.primeva.ModBlockEntities;
import dev.heeystuuh.primeva.ModDataMaps;
import dev.heeystuuh.primeva.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class DistillerBlock extends BaseEntityBlock {
    public static final MapCodec<DistillerBlock> CODEC = simpleCodec(DistillerBlock::new);

    public DistillerBlock(Properties props) {
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
        return new DistillerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.DISTILLER.get(), DistillerBlockEntity::serverTick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        boolean handled = stack.is(ModItems.EMPTY_PHIAL.get()) || stack.is(ModItems.LENS.get())
                || !ModDataMaps.of(stack.getItem()).isEmpty();
        if (!handled) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof DistillerBlockEntity be)) return ItemInteractionResult.FAIL;
        if (stack.is(ModItems.EMPTY_PHIAL.get())) be.fillPhial(player, stack);
        else if (stack.is(ModItems.LENS.get())) be.report(player);
        else be.insert(player, stack);
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof DistillerBlockEntity be) {
            if (player.isShiftKeyDown()) be.extract(player);
            else be.report(player);
        }
        return InteractionResult.CONSUME;
    }
}
