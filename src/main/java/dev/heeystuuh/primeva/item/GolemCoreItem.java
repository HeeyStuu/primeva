package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.entity.ArcaneGolem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Núcleo de Golem: clique num golem para instalar uma especialização (um núcleo por golem). */
public class GolemCoreItem extends Item {
    public final ArcaneGolem.Core core;

    public GolemCoreItem(ArcaneGolem.Core core, Properties p) {
        super(p);
        this.core = core;
    }

    @Override
    public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
        t.add(Component.translatable("tooltip.primeva.golem_core_" + core.name().toLowerCase()).withStyle(ChatFormatting.GRAY));
        if (core != ArcaneGolem.Core.NONE) t.add(Component.translatable("tooltip.primeva.golem_core_use").withStyle(ChatFormatting.DARK_GRAY));
    }
}
