package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.Essence;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Essência Cristalizada: componente avançado (capítulos II e III). */
public class CrystalItem extends Item {
    public final Essence essence;

    public CrystalItem(Essence essence, Properties props) {
        super(props);
        this.essence = essence;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.primeva.crystal").withStyle(ChatFormatting.GRAY));
    }
}
