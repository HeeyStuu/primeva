package dev.heeystuuh.primeva.client;

import dev.heeystuuh.primeva.ClientKnowledge;
import dev.heeystuuh.primeva.Essence;
import dev.heeystuuh.primeva.ModDataMaps;
import dev.heeystuuh.primeva.PrimevaMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Map;

/** Mostra as essências na tooltip dos itens que o jogador já percebeu com a Lente. */
@EventBusSubscriber(modid = PrimevaMod.MODID, value = Dist.CLIENT)
public final class ClientGameEvents {
    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        if (event.getEntity() == null) return;
        if (event.getItemStack().is(dev.heeystuuh.primeva.ModItems.ESSENCE_JAR.get())) {
            net.minecraft.world.item.component.CustomData d = event.getItemStack().get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
            if (d != null) {
                net.minecraft.nbt.CompoundTag t = d.copyTag();
                Essence e = Essence.byId(t.getString("essence"));
                if (e != null) event.getToolTip().add(e.displayName().copy().append(Component.literal(" " + t.getInt("amount")).withStyle(ChatFormatting.WHITE)));
            }
        }
        String set = setKey(event.getItemStack());
        if (set != null) {
            event.getToolTip().add(Component.translatable("tooltip.primeva.set_bonus").withStyle(ChatFormatting.GOLD));
            event.getToolTip().add(Component.translatable("tooltip.primeva.set_" + set).withStyle(ChatFormatting.GRAY));
        }
        Map<Essence, Integer> ess = ModDataMaps.of(event.getItemStack().getItem());
        if (ess.isEmpty()) return;
        String key = "item:" + BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (ClientKnowledge.get().scanned.contains(key)) {
            event.getToolTip().add(Essence.line(ess, ""));
        } else {
            event.getToolTip().add(Component.translatable("tooltip.primeva.unknown").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static String setKey(net.minecraft.world.item.ItemStack s) {
        net.minecraft.resources.ResourceLocation id = BuiltInRegistries.ITEM.getKey(s.getItem());
        if (!id.getNamespace().equals(PrimevaMod.MODID)) return null;
        String p = id.getPath();
        for (String m : new String[]{"primeval", "arcanite", "crimson", "voidsteel"}) {
            for (String t : new String[]{"_helmet", "_chestplate", "_leggings", "_boots"}) if (p.equals(m + t)) return m;
        }
        return null;
    }

    private ClientGameEvents() {}
}
