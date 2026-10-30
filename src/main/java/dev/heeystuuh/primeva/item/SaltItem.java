package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.ModBlocks;
import dev.heeystuuh.primeva.ModItems;
import dev.heeystuuh.primeva.block.CrucibleBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Sal Primordial: polvilhe numa estante para criar um Grimório, ou num caldeirão com água para criar um Cadinho Arcano. */
public class SaltItem extends Item {
    public SaltItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState st = level.getBlockState(pos);
        Player player = ctx.getPlayer();
        boolean shelf = st.is(Blocks.BOOKSHELF);
        boolean cauldron = st.is(Blocks.WATER_CAULDRON);
        if (!shelf && !cauldron) return InteractionResult.PASS;
        if (level instanceof ServerLevel sl) {
            if (shelf) {
                sl.removeBlock(pos, false);
                ItemEntity book = new ItemEntity(sl, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.GRIMOIRE.get()));
                book.setDeltaMovement(0, 0.2, 0);
                sl.addFreshEntity(book);
            } else {
                sl.setBlockAndUpdate(pos, ModBlocks.CRUCIBLE.get().defaultBlockState());
                if (sl.getBlockEntity(pos) instanceof CrucibleBlockEntity be) be.fill();
            }
            sl.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 60, 0.4, 0.5, 0.4, 0.8);
            sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.4, 0.4, 0.4, 0.05);
            sl.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1f, 1.2f);
            sl.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1f, 1f);
            if (player == null || !player.getAbilities().instabuild) ctx.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.primeva.salt_1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.primeva.salt_2").withStyle(ChatFormatting.GRAY));
    }
}
