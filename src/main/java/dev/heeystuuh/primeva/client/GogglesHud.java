package dev.heeystuuh.primeva.client;

import dev.heeystuuh.primeva.ClientKnowledge;
import dev.heeystuuh.primeva.Essence;
import dev.heeystuuh.primeva.ModItems;
import dev.heeystuuh.primeva.PrimevaMod;
import dev.heeystuuh.primeva.ScanTarget;
import dev.heeystuuh.primeva.item.LensItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.Map;

/** Óculos da Revelação: mostra as essências do que você está olhando (se já foi estudado com a Lente). */
public final class GogglesHud {
    private static final ResourceLocation ESS = PrimevaMod.id("textures/gui/essences.png");

    @EventBusSubscriber(modid = PrimevaMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Layers {
        @SubscribeEvent
        public static void register(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.CROSSHAIR, PrimevaMod.id("goggles"), GogglesHud::render);
        }
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if (p == null || mc.options.hideGui || mc.screen != null) return;
        if (!p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.REVEALING_GOGGLES.get())) return;
        if (p.isUsingItem() && p.getUseItem().getItem() instanceof LensItem) return;
        if (machineInfo(g, mc)) return;
        ScanTarget t = ScanTarget.find(p);
        if (t == null) return;
        Font font = mc.font;
        int cx = g.guiWidth() / 2, y = g.guiHeight() / 2 + 12;
        boolean known = ClientKnowledge.get().scanned.contains(t.key());
        Component name = t.name().copy().withStyle(ChatFormatting.WHITE);
        g.drawCenteredString(font, name, cx, y, 0xFFFFFF);
        y += 11;
        if (t.essences().isEmpty()) {
            g.drawCenteredString(font, Component.translatable("gui.primeva.goggles_none").withStyle(ChatFormatting.DARK_GRAY), cx, y, 0xFFFFFF);
            return;
        }
        int n = t.essences().size();
        int w = n * 26;
        int x = cx - w / 2;
        for (Map.Entry<Essence, Integer> e : t.essences().entrySet()) {
            g.blit(ESS, x + 2, y, 12, 12, e.getKey().ordinal() * 32, 0, 32, 32, 192, 32);
            String txt = known ? String.valueOf(e.getValue()) : "?";
            g.drawString(font, txt, x + 15, y + 2, known ? 0xFFFFFF : 0x9A9A9A, true);
            x += 26;
        }
    }

    /** Indicadores das máquinas: conteúdo de jarros, cadinho e destilador. */
    private static boolean machineInfo(GuiGraphics g, Minecraft mc) {
        if (!(mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult bh) || mc.hitResult.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK || mc.level == null) return false;
        net.minecraft.world.level.block.entity.BlockEntity be = mc.level.getBlockEntity(bh.getBlockPos());
        Map<Essence, Integer> ess = null;
        Component title = null;
        Component extra = null;
        if (be instanceof dev.heeystuuh.primeva.block.EssenceJarBlockEntity jar) {
            title = Component.translatable("block.primeva.essence_jar");
            ess = new java.util.EnumMap<>(Essence.class);
            if (jar.essence() != null) ess.put(jar.essence(), jar.amount());
            extra = Component.literal(jar.amount() + "/" + dev.heeystuuh.primeva.block.EssenceJarBlockEntity.capacity());
        } else if (be instanceof dev.heeystuuh.primeva.block.CrucibleBlockEntity cr) {
            title = Component.translatable("block.primeva.crucible");
            ess = cr.essences();
            extra = cr.hasWater() ? Component.literal(cr.total() + "/" + dev.heeystuuh.primeva.block.CrucibleBlockEntity.capacity())
                    : Component.translatable("msg.primeva.crucible_no_water");
        } else if (be instanceof dev.heeystuuh.primeva.block.DistillerBlockEntity d) {
            title = Component.translatable("block.primeva.distiller");
            ess = d.stored();
        }
        if (title == null) return false;
        Font font = mc.font;
        int cx = g.guiWidth() / 2, y = g.guiHeight() / 2 + 12;
        g.drawCenteredString(font, title, cx, y, 0xFFD860);
        y += 11;
        if (extra != null) {
            g.drawCenteredString(font, extra, cx, y, 0xC0C0C0);
            y += 11;
        }
        if (ess == null || ess.isEmpty()) {
            g.drawCenteredString(font, Component.translatable("msg.primeva.empty"), cx, y, 0x9A9A9A);
            return true;
        }
        int x = cx - ess.size() * 30 / 2;
        for (Map.Entry<Essence, Integer> e : ess.entrySet()) {
            g.blit(ESS, x + 2, y, 12, 12, e.getKey().ordinal() * 32, 0, 32, 32, 192, 32);
            g.drawString(font, String.valueOf(e.getValue()), x + 15, y + 2, 0xFFFFFF, true);
            x += 30;
        }
        return true;
    }

    private GogglesHud() {}
}
