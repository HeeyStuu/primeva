package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.entity.ArcaneGolem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Selo: clique num Golem Arcano para dar a ele uma tarefa. */
public class SealItem extends Item {
    public final ArcaneGolem.Task task;

    public SealItem(ArcaneGolem.Task task, Properties p) {
        super(p);
        this.task = task;
    }

    @Override
    public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
        t.add(Component.translatable("tooltip.primeva.seal_" + task.name().toLowerCase()).withStyle(ChatFormatting.GRAY));
        t.add(Component.translatable("tooltip.primeva.seal_use").withStyle(ChatFormatting.DARK_GRAY));
    }
}
