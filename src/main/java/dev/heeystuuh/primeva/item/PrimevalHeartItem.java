package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.CommonEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Coração Primevo: consumido para a Ascensão (+10 de vida permanente, imune a miasma, +50% de percepção). */
public class PrimevalHeartItem extends Item {
    public PrimevalHeartItem(Properties props) {
        super(props);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer sp) {
            if (CommonEvents.isAscended(sp)) {
                sp.displayClientMessage(Component.translatable("msg.primeva.already_ascended").withStyle(ChatFormatting.GRAY), true);
                return InteractionResultHolder.fail(stack);
            }
            CommonEvents.ascend(sp);
            if (!sp.getAbilities().instabuild) stack.shrink(1);
            ServerLevel sl = sp.serverLevel();
            sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, sp.getX(), sp.getY() + 1, sp.getZ(), 200, 0.6, 1.2, 0.6, 0.6);
            sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1, sp.getZ(), 120, 1.5, 2, 1.5, 0.1);
            level.playSound(null, sp.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1f, 0.8f);
            level.playSound(null, sp.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1f, 0.5f);
            sp.server.getPlayerList().broadcastSystemMessage(Component.translatable("msg.primeva.ascended",
                    sp.getDisplayName().copy().withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GOLD), false);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.primeva.primeval_heart.1").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.primeva.primeval_heart.2").withStyle(ChatFormatting.GRAY));
    }
}
