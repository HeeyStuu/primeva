package dev.heeystuuh.primeva.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Item simples com uma linha de dica (tooltip.primeva.<chave>). */
public class TooltipItem extends Item {
    private final String key;
    private final boolean glint;

    public TooltipItem(String key, boolean glint, Properties props) {
        super(props);
        this.key = key;
        this.glint = glint;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return glint || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.primeva." + key).withStyle(ChatFormatting.GRAY));
    }
}
