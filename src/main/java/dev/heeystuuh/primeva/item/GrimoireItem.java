package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.client.ClientHooks;
import dev.heeystuuh.primeva.network.Network;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Grimório Primevo: abre a tela de pesquisas. Também ativa o Altar de Infusão. */
public class GrimoireItem extends Item {
    public GrimoireItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            ClientHooks.openGrimoire();
        } else if (player instanceof ServerPlayer sp) {
            Network.sync(sp);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.primeva.grimoire.1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.primeva.grimoire.2").withStyle(ChatFormatting.GRAY));
    }
}
