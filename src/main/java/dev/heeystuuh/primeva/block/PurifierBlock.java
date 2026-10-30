package dev.heeystuuh.primeva.block;

import com.mojang.serialization.MapCodec;
import dev.heeystuuh.primeva.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PurifierBlock extends BaseEntityBlock {
    public static final MapCodec<PurifierBlock> CODEC = simpleCodec(PurifierBlock::new);

    public PurifierBlock(Properties props) {
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
        return new PurifierBlockEntity(pos, state);
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                                  net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand,
                                                                  net.minecraft.world.phys.BlockHitResult hit) {
        if (!stack.is(dev.heeystuuh.primeva.ModItems.phial(dev.heeystuuh.primeva.Essence.LUMEN))) return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PurifierBlockEntity be) {
            be.addBoost(20 * 120);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                net.minecraft.world.item.ItemStack empty = new net.minecraft.world.item.ItemStack(dev.heeystuuh.primeva.ModItems.EMPTY_PHIAL.get());
                if (!player.getInventory().add(empty)) player.drop(empty, false);
            }
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BEACON_POWER_SELECT, net.minecraft.sounds.SoundSource.BLOCKS, 1f, 1.4f);
            if (player instanceof net.minecraft.server.level.ServerPlayer sp) dev.heeystuuh.primeva.Discovery.grant(sp, "do:purify");
        }
        return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player,
                                                                   net.minecraft.world.phys.BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PurifierBlockEntity be) {
            int m = dev.heeystuuh.primeva.Miasma.get(level, pos);
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("msg.primeva.purifier_status",
                    dev.heeystuuh.primeva.Miasma.describe(m), be.boost() / 20), true);
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.PURIFIER.get(), PurifierBlockEntity::serverTick);
    }
}
