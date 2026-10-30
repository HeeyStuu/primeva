package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.Essence;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Frasco cheio com 5 pontos de uma essência. */
public class PhialItem extends Item {
    public static final int AMOUNT = 5;
    public final Essence essence;

    public PhialItem(Essence essence, Properties props) {
        super(props);
        this.essence = essence;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(AMOUNT + " ").withStyle(essence.fmt).append(essence.displayName()));
        tooltip.add(Component.translatable("tooltip.primeva.phial").withStyle(ChatFormatting.GRAY));
    }
}
