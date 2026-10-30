package dev.heeystuuh.primeva.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Picareta Primeva: minera em área 3x3 (agachado minera um bloco só). A lógica fica em CommonEvents. */
public class PrimevalPickaxeItem extends PickaxeItem {
    public PrimevalPickaxeItem(Tier tier, Properties props) {
        super(tier, props);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.primeva.primeval_pickaxe").withStyle(ChatFormatting.AQUA));
    }
}
