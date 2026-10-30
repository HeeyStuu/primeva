package dev.heeystuuh.primeva.block;

import dev.heeystuuh.primeva.Essence;
import dev.heeystuuh.primeva.item.PhialItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Tubo Filtrante: só deixa passar uma essência. Clique com um frasco para escolher; mão vazia limpa o filtro. */
public class EssenceFilterTubeBlock extends EssenceTubeBlock {
    public static final IntegerProperty FILTER = IntegerProperty.create("filter", 0, 6);

    public EssenceFilterTubeBlock(Properties props) {
        super(props);
        registerDefaultState(defaultBlockState().setValue(FILTER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        super.createBlockStateDefinition(b);
        b.add(FILTER);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(stack.getItem() instanceof PhialItem p)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!level.isClientSide) {
            level.setBlockAndUpdate(pos, state.setValue(FILTER, p.essence.ordinal() + 1));
            player.displayClientMessage(Component.translatable("msg.primeva.filter_set", p.essence.displayName()).withStyle(ChatFormatting.AQUA), true);
            level.playSound(null, pos, SoundEvents.COPPER_PLACE, SoundSource.BLOCKS, 1f, 1.4f);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                level.setBlockAndUpdate(pos, state.setValue(FILTER, 0));
                player.displayClientMessage(Component.translatable("msg.primeva.filter_cleared").withStyle(ChatFormatting.GRAY), true);
            } else {
                int f = state.getValue(FILTER);
                player.displayClientMessage(f == 0 ? Component.translatable("msg.primeva.filter_none").withStyle(ChatFormatting.GRAY)
                        : Component.translatable("msg.primeva.filter_set", Essence.values()[f - 1].displayName()).withStyle(ChatFormatting.AQUA), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
